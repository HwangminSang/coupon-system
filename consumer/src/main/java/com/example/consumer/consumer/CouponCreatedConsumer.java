package com.example.consumer.consumer;


import com.example.consumer.domain.Coupon;
import com.example.consumer.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponCreatedConsumer {

    private final CouponRepository couponRepository;


    /**
     * 프로듀서를 통해 topic에 들어온 사용자의 아이디를 컨슈머에서 가져와 db에 insert 한다.
     * 분산이벤트스트리밍서비스 이용  ( API를 통해 데이터베이스에 바로 INSERT되는 부하를 줄일수는 있지만(처리량 조절을 통해 ) 약간의 텀이 생김 )
     * @param userId
     */

     @KafkaListener(topics = "coupon_new_create" , groupId = "foo")
     public void listener(String userId){


         System.out.println("user_ID" + " = " + userId);

         couponRepository.save(new Coupon(Long.valueOf(userId)));

     }
}
