package com.example.api.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
@Getter
@Table(name = "TB_COUPON")
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
