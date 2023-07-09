package com.example.api.producer;


import com.example.api.service.ApplyService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponCreateProducer {


    private final KafkaTemplate<String,String> kafkaTemplate;

    private final Logger logger = LoggerFactory.getLogger(CouponCreateProducer.class);


    public void createCoupon(String userId){

        logger.info("카프카 프로듀서를 이용하여 토픽에 전송중인 아이디 = " + userId );
        // 해당 coupon_create ( topic )으로 유저의 아이디를 보내준다.
        kafkaTemplate.send("coupon_new_create" , userId);
    }
}
