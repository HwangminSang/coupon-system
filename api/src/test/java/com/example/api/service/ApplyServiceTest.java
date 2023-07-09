package com.example.api.service;

import com.example.api.repository.CouponRedisSupportRepository;
import com.example.api.repository.CouponRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForClassTypes.*;


@SpringBootTest
class ApplyServiceTest {


     @Autowired
     private ApplyService applyService;

     @Autowired
     private CouponRepository couponRepository;


     @Autowired
     private CouponRedisSupportRepository couponRedisSupportRepository;

    @Test
     public void 한번만응모(){

         //given
         applyService.apply(1L);

         //when
         long totalCount = couponRepository.count();

         //then
         assertThat(totalCount).isEqualTo(1);
     }


    /**
     * expected: 100L
     *  but was: 105L
     *  테스트 결과 실패
     *  원인 : 레이스 컨디션 발생 ( 두개의 스레드가 하나의 공유자원에 엑세스후 작업시 발생 )
     *  해결 방법 : db 락 이용 ( insert시 오래 걸린다면?? 효율적이지 못하다 )
     *            자바의 시크로나이즈 사용 ( 서버가 여러대일경우? 해결 x )
     *            레디스 이용 ( 레디스는 싱글스레드 기반 그리고 incr명령어를 이용한다면 빠르다 ( key :value )
     *            or 카프카 이용
     * @throws InterruptedException
     */
    @Test
    public void 여러번응모() throws InterruptedException {

         // 1000개의 요청을 보낸다.
         int threadCount = 1000;
         // 동시에 여러개의 요청을 보내기 위해 멀티스레드 환경 구성 (병렬 작업 )
        ExecutorService executorService = Executors.newFixedThreadPool(32);

        // 모든 요청이 끝날때까지 기다리기 위해 CountDowLatch 사용
        // 다른 스레드에서 작업되고 있는 내용을 기다려주게 하는 클래스
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 1000개의 요청을 위한 로직
        for(int i = 0; i < threadCount; i++){
            long userId  = i;
            executorService.submit(()->{
                try{
                    applyService.apply(userId);

                }finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        //when
        long totalCount = couponRepository.count();

        //then
        assertThat(totalCount).isEqualTo(100);
    }


    /**
     * 레이스 컨디션 발생 ( 병렬 작업시 )
     * 해결방안
     * 1. 레디스를 이용하여 카운트 조회후 100개가 넘으면 발급 x
     *
     */
    @Test
    public void 레디스이용레이스컨디션해결() throws InterruptedException {


        // 1000개의 요청을 보낸다.
        int threadCount = 1000;
        // 동시에 여러개의 요청을 보내기 위해 멀티스레드 환경 구성 (병렬 작업 )
        ExecutorService executorService = Executors.newFixedThreadPool(32);

        // 모든 요청이 끝날때까지 기다리기 위해 CountDowLatch 사용
        // 다른 스레드에서 작업되고 있는 내용을 기다려주게 하는 클래스
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 1000개의 요청을 위한 로직
        for(int i = 0; i < threadCount; i++){
            long userId  = i;
            executorService.submit(()->{
                try{
                    // 디비에 바로 넣는다
//                    applyService.apply(userId);
                    // 레디스를 이용하여 레이스 컨디션 해결하지만 디비에 부하가 갈수가 있다.
//                    applyService.applyRedis(userId);
                    // 카프카를 이용하여 특정 topic에 넣어둔뒤 순차적으로 컨슈머가 가져가 db에 insert 한다
                    applyService.applyKafka(userId);
                }finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        /**
         *  테스트를 위하여 스레드 10초 지연.
         *  1. 테스트 진행 ~ 종료 3초
         *  2. 프로듀서를 통해 해당 토픽에 데이터를 넣은 후 컨슈머에서 가져와 DB에 INSERT시 5초
         *  3. 결과적으로 데이터베이스에는 100개의 쿠폰을 받은 유저의 정보가 들어가지만 테스트는 그전에 끝나기 때문에  성공 X
         *  4. 따라서 DB에서 COUNT를 가져오는것은 10초 지연시켜 테스트 여부 확인
         */

        Thread.sleep(1000);
        //when
        long totalCount = couponRepository.count();

        //then
        assertThat(totalCount).isEqualTo(100);
    }


    /**
     * 테스트 상황
     * 1. 1이라는 유저가 1000번의 요청을 보내도 1번만 발급되는지 확인!
     * @throws InterruptedException
     */
    @Test
    public void 한명당_하나의쿠폰발급() throws InterruptedException {

        // 유저아이디 고정값
        long userId  = 1L;


        // 1000개의 요청을 보낸다.
        int threadCount = 1000;
        // 동시에 여러개의 요청을 보내기 위해 멀티스레드 환경 구성 (병렬 작업 )
        ExecutorService executorService = Executors.newFixedThreadPool(32);

        // 모든 요청이 끝날때까지 기다리기 위해 CountDowLatch 사용
        // 다른 스레드에서 작업되고 있는 내용을 기다려주게 하는 클래스
        CountDownLatch latch = new CountDownLatch(threadCount);
        // 1000개의 요청을 위한 로직
        for(int i = 0; i < threadCount; i++){

            executorService.submit(()->{
                try{

                    applyService.applyKafka(userId);
                }finally {
                    latch.countDown();
                }
            });
        }

        latch.await();


        long totalCount = couponRepository.count();


        assertThat(totalCount).isEqualTo(1);
    }

}