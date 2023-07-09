package com.example.api.producer;


import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponCreateProducer {


    private final KafkaTemplate<String,String> kafkaTemplate;


    public void createCoupon(String userId){


        // 해당 coupon_create ( topic )으로 유저의 아이디를 보내준다.
        kafkaTemplate.send("coupon_new_create" , userId);
    }
}
