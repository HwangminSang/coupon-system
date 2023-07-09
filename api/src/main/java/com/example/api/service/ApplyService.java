package com.example.api.service;


import com.example.api.domain.Coupon;
import com.example.api.producer.CouponCreateProducer;
import com.example.api.repository.CouponRedisSupportRepository;
import com.example.api.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.plaf.PanelUI;

@Service
@RequiredArgsConstructor
@Transactional
public class ApplyService {

    //일반 레포지토리
   private final CouponRepository couponRepository;


   //레디스 레포지토리
   private final CouponRedisSupportRepository couponRedisSupportRepository;

   // 카프카템플릿
    private final CouponCreateProducer couponCreateProducer;

    private final Logger logger = LoggerFactory.getLogger(ApplyService.class);


    /**
    * 쿠폰발급 조건
    * 1. 100개를 초과해서 발급 x
    * 2. 쿠폰테이블에서 쿠폰의 총 갯수를 가져온다
    * 3. 해당 갯수가 100개를 초과하였는지 확인한다.
    * 4. 100개를 초과했을경우 발급 x  100개 미만일경우 받아온 사용자의 아이디와 함께 쿠폰 엔티티를 생성하여 insert 한다.
    *
    * 결과 : 동시에 여러개의 요청이 있을경우 스레드간  레이스 컨디션 발생.
    */

   public void apply(Long userId){
      long totalCount = couponRepository.count();

       if(totalCount > 100 ){
          return ;
       }


       couponRepository.save(new Coupon(userId));
   }


    /**
     * 해결방안 : 레디스 이용
     * 1. 레디스를 이용하여 쿠폰을 발급하기전 count를 증가시켜 확인
     * 2. 레디스는 싱글스레드 환경 ( 레이스 컨디션 해결 가능 )
     * 3. 다만 mysql에 1분에 100개만 insert 할수 있는 상황에서 100개의 요청을 처리하는 동안 회원가입 요청이 온다면?
     * 4. 다른 작업의 요청을 빠르게 해결하지 못할 가능성이 있다. ( 쿠폰 DB만 따로 있는 상태가 X )
     * 5. 그래서 해당 요청사항을 메세지 큐 서비스를 이용하여 처리를 하자! ( db에 바로 insert x  , 큐에 넣어둔뒤 순차적으로 처리 )
     * @param userId
     */
    public void applyRedis(Long userId){
        long totalCount = couponRedisSupportRepository.applyCouponCountIncrement();

        if(totalCount > 100 ){
            return ;
        }


        couponRepository.save(new Coupon(userId));
    }

    /**
     * 카프카이용 ( 이벤트 큐 )
     * 1. 한명이 여러개의 쿠폰을 가질수 없다! 1개만 발급되도록 하기위해 REDIS의 SET 구조 이용
     * 2. 레디스를 이용하여 멀티스레드 환경에서 레이스 컨디션 방지
     * 3. db에 바로 insert하지 않고 ( 메세지 큐 서비스 이용하여 순차적으로 디비에 넣는다 )
     * 4. 프로듀서를 이용하여 특정 topic에 넣어둔뒤 컨슈머에서 가져간다.
     */

    public void applyKafka(Long userId){

        // 쿠폰을 이미 발급받은 유저인지 체크
        Long checkUser = couponRedisSupportRepository.checkAppliedUser(userId);

        if(checkUser != 1 ){
            logger.error("The user has already been issued a coupon");
            return;
        }

        // 쿠폰 발급 가능한 총 갯수를 초과하였는지 체크
        long totalCount = couponRedisSupportRepository.applyCouponCountIncrement();

        if(totalCount > 100 ){
            logger.error("The number of coupons that can be issued has exceeded 100");
            return ;
        }
        couponCreateProducer.createCoupon(userId.toString());
    }
}
