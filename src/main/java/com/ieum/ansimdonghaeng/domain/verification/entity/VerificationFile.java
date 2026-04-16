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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "VERIFICATION_ID", nullable = false)
    private Verification verification;

    @Column(name = "ORIGINAL_NAME", nullable = false, length = 255)
    private String originalName;

    @Column(name = "STORED_NAME", nullable = false, length = 255)
    private String storedName;

    @Column(name = "FILE_URL", nullable = false, length = 2000)
    private String fileUrl;

    @Column(name = "CONTENT_TYPE", length = 100)
    private String contentType;

    @Column(name = "FILE_SIZE", nullable = false)
    private Long fileSize;

    @Column(name = "UPLOADED_AT", nullable = false)
    private LocalDateTime uploadedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private VerificationFile(Verification verification,
                             String originalName,
                             String storedName,
                             String fileUrl,
                             String contentType,
                             Long fileSize,
                             LocalDateTime uploadedAt) {
        this.verification = verification;
        this.originalName = originalName;
        this.storedName = storedName;
        this.fileUrl = fileUrl;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.uploadedAt = uploadedAt;
    }

    public static VerificationFile create(Verification verification,
                                          String originalName,
                                          String storedName,
                                          String fileUrl,
                                          String contentType,
                                          Long fileSize,
                                          LocalDateTime uploadedAt) {
        return VerificationFile.builder()
                .verification(verification)
                .originalName(originalName)
                .storedName(storedName)
                .fileUrl(fileUrl)
                .contentType(contentType)
                .fileSize(fileSize)
                .uploadedAt(uploadedAt)
                .build();
    }
}
