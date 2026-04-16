package com.ieum.ansimdonghaeng.domain.freelancer.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "PORTFOLIO_FILE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(name = "portfolio_file_seq_generator", sequenceName = "SEQ_PORT_FILE", allocationSize = 1)
public class FreelancerFile {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "portfolio_file_seq_generator")
    @Column(name = "PORTFOLIO_FILE_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FREELANCER_PROFILE_ID", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private FreelancerProfile freelancerProfile;

    @Column(name = "ORIGINAL_NAME", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "STORED_NAME", nullable = false, length = 255)
    private String storedFilename;

    @Column(name = "FILE_URL", nullable = false, length = 500)
    private String fileUrl;

    @Column(name = "CONTENT_TYPE", length = 100)
    private String contentType;

    @Column(name = "FILE_SIZE", nullable = false)
    private Long fileSize;

    @Column(name = "DISPLAY_ORDER", nullable = false)
    private Integer displayOrder;

    @Column(name = "UPLOADED_AT", nullable = false)
    private LocalDateTime uploadedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private FreelancerFile(FreelancerProfile freelancerProfile,
                           String originalFilename,
                           String storedFilename,
                           String fileUrl,
                           String contentType,
                           Long fileSize,
                           Integer displayOrder,
                           LocalDateTime uploadedAt) {
        this.freelancerProfile = freelancerProfile;
        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.fileUrl = fileUrl;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.displayOrder = displayOrder;
        this.uploadedAt = uploadedAt;
    }

    public static FreelancerFile create(FreelancerProfile freelancerProfile,
                                        String originalFilename,
                                        String storedFilename,
                                        String fileUrl,
                                        String contentType,
                                        Long fileSize,
                                        Integer displayOrder) {
        return FreelancerFile.builder()
                .freelancerProfile(freelancerProfile)
                .originalFilename(originalFilename)
                .storedFilename(storedFilename)
                .fileUrl(fileUrl)
                .contentType(contentType)
                .fileSize(fileSize)
                .displayOrder(displayOrder)
                .uploadedAt(LocalDateTime.now())
                .build();
    }

    public boolean isOwnedBy(Long userId) {
        return Objects.equals(freelancerProfile.getUser().getId(), userId);
    }
}
