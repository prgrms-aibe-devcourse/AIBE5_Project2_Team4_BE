package com.ieum.ansimdonghaeng.domain.code.repository;

import com.ieum.ansimdonghaeng.domain.code.entity.ProjectTypeCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectTypeCodeRepository extends JpaRepository<ProjectTypeCode, String> {

    boolean existsByCodeAndActiveYnTrue(String code);
}
