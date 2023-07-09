package com.example.consumer.consumer;


import com.example.consumer.domain.Coupon;
import com.example.consumer.domain.FailedCouponEvent;
import com.example.consumer.repository.CouponRepository;
import com.example.consumer.repository.FailedCouponEventRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponCreatedConsumer {

    private final CouponRepository couponRepository;

    private final FailedCouponEventRepository failedCouponEventRepository;

    private final Logger logger = LoggerFactory.getLogger(CouponCreatedConsumer.class);

    /**
     * 프로듀서를 통해 topic에 들어온 사용자의 아이디를 컨슈머에서 가져와 db에 insert 한다.
     * 분산이벤트스트리밍서비스 이용  ( API를 통해 데이터베이스에 바로 INSERT되는 부하를 줄일수는 있지만(처리량 조절을 통해 ) 약간의 텀이 생김 )
     * 쿠폰 발급실패시 로그 전용 엔티티를 만들어 남긴다.
     * @param userId
     */

     @KafkaListener(topics = "coupon_new_create" , groupId = "foo")
     public void listener(String userId){

         try {

//             throw  new Exception("실패테스트");


             couponRepository.save(new Coupon(Long.valueOf(userId)));
             logger.info("쿠폰 발급 성공 userId = " + userId);

         }catch (Exception e){

             logger.error("쿠폰 발급에 실패하였습니다 :: " + userId);

             FailedCouponEvent failedCouponEvent = FailedCouponEvent.builder()
                     .reason(e.getMessage())
                     .userId(Long.valueOf(userId))
                     .build();
             failedCouponEventRepository.save(failedCouponEvent);

         }

     }
}
