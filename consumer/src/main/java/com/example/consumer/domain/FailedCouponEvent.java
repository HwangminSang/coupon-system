package com.example.consumer.domain;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TB_FAILED_COUPON_EVENT")
@NoArgsConstructor
public class FailedCouponEvent {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String reason;


    @Builder
    public FailedCouponEvent(Long userId ,String reason){
        this.userId = userId;
        this.reason = reason;
    }
}
