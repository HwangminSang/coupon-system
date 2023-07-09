package com.example.api.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CouponRedisSupportRepository {

    private final static String COUPON_COUNT = "COUPON_COUNT";

    private final static String COUPON_APPLIED_USER = "COUPON_APPLIED_USER";


    // 레디스를 이용하여 쿠폰의 카운트를 조회.
    private final RedisTemplate<String ,String > redisTemplate;


    // 레디스 이용  쿠폰 발급시 coupon_count라는 key값으로 1씩 value가 증가하도록 설정.
    public Long applyCouponCountIncrement(){
        return redisTemplate
                .opsForValue()
                .increment(COUPON_COUNT);
    }

    public Long checkAppliedUser(Long userId){
        return redisTemplate
                .opsForSet()
                .add(COUPON_APPLIED_USER , userId.toString());
    }

}
