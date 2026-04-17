package com.ieum.ansimdonghaeng.domain.verification.entity;

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
@Table(name = "VERIFICATION_FILE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(name = "verification_file_seq_generator", sequenceName = "SEQ_VER_FILE", allocationSize = 1)
public class VerificationFile {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "verification_file_seq_generator")
    @Column(name = "VERIFICATION_FILE_ID")
    private Long id;

    @Column(name = "VERIFICATION_ID", nullable = false)
    private Long verificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VERIFICATION_ID", insertable = false, updatable = false)
    private Verification verification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VERIFICATION_ID", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private VerificationRequest verificationRequest;

    @Column(name = "ORIGINAL_NAME", nullable = false, length = 510)
    private String originalFilename;

    @Column(name = "STORED_NAME", nullable = false, length = 510)
    private String storedFilename;

    @Column(name = "FILE_URL", nullable = false, length = 2000)
    private String fileUrl;

    @Column(name = "CONTENT_TYPE", length = 200)
    private String contentType;

    @Column(name = "FILE_SIZE", nullable = false)
    private Long fileSize;

    @Column(name = "UPLOADED_AT", nullable = false)
    private LocalDateTime uploadedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private VerificationFile(Long verificationId,
                             Verification verification,
                             VerificationRequest verificationRequest,
                             String originalFilename,
                             String storedFilename,
                             String fileUrl,
                             String contentType,
                             Long fileSize,
                             LocalDateTime uploadedAt) {
        this.verificationId = verificationId;
        this.verification = verification;
        this.verificationRequest = verificationRequest;
        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.fileUrl = fileUrl;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.uploadedAt = uploadedAt;
    }

    public static VerificationFile create(Verification verification,
                                          String originalFilename,
                                          String storedFilename,
                                          String fileUrl,
                                          String contentType,
                                          Long fileSize,
                                          LocalDateTime uploadedAt) {
        return VerificationFile.builder()
                .verificationId(verification.getId())
                .verification(verification)
                .originalFilename(originalFilename)
                .storedFilename(storedFilename)
                .fileUrl(fileUrl)
                .contentType(contentType)
                .fileSize(fileSize)
                .uploadedAt(uploadedAt)
                .build();
    }

    public static VerificationFile create(VerificationRequest verificationRequest,
                                          String originalFilename,
                                          String storedFilename,
                                          String fileUrl,
                                          String contentType,
                                          Long fileSize) {
        return VerificationFile.builder()
                .verificationId(verificationRequest.getId())
                .verificationRequest(verificationRequest)
                .originalFilename(originalFilename)
                .storedFilename(storedFilename)
                .fileUrl(fileUrl)
                .contentType(contentType)
                .fileSize(fileSize)
                .uploadedAt(LocalDateTime.now())
                .build();
    }

    public boolean isOwnedBy(Long userId) {
        return Objects.equals(verificationRequest.getFreelancerProfile().getUser().getId(), userId);
    }

    public String getOriginalName() {
        return originalFilename;
    }

    public String getStoredName() {
        return storedFilename;
    }
}
