package com.example.consumer.repository;

import com.example.consumer.domain.FailedCouponEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FailedCouponEventRepository extends JpaRepository<FailedCouponEvent , Long> {

}
