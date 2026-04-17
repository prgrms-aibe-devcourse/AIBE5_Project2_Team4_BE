package com.ieum.ansimdonghaeng.domain.code.repository;

import com.ieum.ansimdonghaeng.domain.code.entity.AvailableTimeSlotCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvailableTimeSlotCodeRepository extends JpaRepository<AvailableTimeSlotCode, String> {

    boolean existsByCodeAndActiveYnTrue(String code);
}
