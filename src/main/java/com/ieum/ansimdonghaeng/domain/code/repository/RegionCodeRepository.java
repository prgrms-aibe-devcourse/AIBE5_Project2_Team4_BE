package com.ieum.ansimdonghaeng.domain.code.repository;

import com.ieum.ansimdonghaeng.domain.code.entity.RegionCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionCodeRepository extends JpaRepository<RegionCode, String> {

    boolean existsByCodeAndActiveYnTrue(String code);
}
