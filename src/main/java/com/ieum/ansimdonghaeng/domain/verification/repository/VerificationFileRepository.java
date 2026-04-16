package com.ieum.ansimdonghaeng.domain.verification.repository;

import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationFile;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VerificationFileRepository extends JpaRepository<VerificationFile, Long> {

    boolean existsByVerification_Id(Long verificationId);

    @Query("select distinct verificationFile.verification.id from VerificationFile verificationFile where verificationFile.verification.id in :verificationIds")
    List<Long> findVerificationIdsWithFiles(@Param("verificationIds") List<Long> verificationIds);

    List<VerificationFile> findAllByVerification_IdOrderByUploadedAtAsc(Long verificationId);
}
