package com.example.consumer.domain;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
@Getter
public class Coupon {


    // 쿠폰 고유의 아이디
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 발급받은 사용자의 아이디
    private Long userId;


    public Coupon(Long userId){
        this.userId = userId;
    }

}
