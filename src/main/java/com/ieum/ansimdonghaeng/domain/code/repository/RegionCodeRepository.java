package com.ieum.ansimdonghaeng.domain.code.repository;

import com.ieum.ansimdonghaeng.domain.code.entity.RegionCode;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionCodeRepository extends JpaRepository<RegionCode, String> {

    boolean existsByCodeAndActiveYnTrue(String code);

    List<RegionCode> findAllByActiveYnTrueOrderByRegionLevelAscNameAscCodeAsc();
}
