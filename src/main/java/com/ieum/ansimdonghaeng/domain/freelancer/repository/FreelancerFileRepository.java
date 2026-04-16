package com.ieum.ansimdonghaeng.domain.freelancer.repository;

import com.ieum.ansimdonghaeng.domain.freelancer.entity.FreelancerFile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FreelancerFileRepository extends JpaRepository<FreelancerFile, Long> {

    @Query("""
            select file
            from FreelancerFile file
            join fetch file.freelancerProfile profile
            join fetch profile.user
            where profile.user.id = :userId
            order by file.displayOrder asc, file.uploadedAt desc
            """)
    List<FreelancerFile> findAllByUserId(@Param("userId") Long userId);

    long countByFreelancerProfile_Id(Long freelancerProfileId);

    @Query("""
            select file
            from FreelancerFile file
            join fetch file.freelancerProfile profile
            join fetch profile.user
            where file.id = :fileId
            """)
    Optional<FreelancerFile> findDetailById(@Param("fileId") Long fileId);
}
