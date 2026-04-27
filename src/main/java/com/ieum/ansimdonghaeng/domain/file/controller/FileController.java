package com.ieum.ansimdonghaeng.domain.file.controller;

import com.ieum.ansimdonghaeng.common.security.CustomUserDetails;
import com.ieum.ansimdonghaeng.domain.file.service.FileService;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileService fileService;

    @GetMapping("/{fileKey}")
    public ResponseEntity<Resource> viewFile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                             @PathVariable String fileKey) {
        return toResponse(fileService.loadInline(userDetails, fileKey));
    }

    @GetMapping("/{fileKey}/download")
    public ResponseEntity<Resource> downloadFile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                 @PathVariable String fileKey) {
        return toResponse(fileService.loadDownload(userDetails, fileKey));
    }

    private ResponseEntity<Resource> toResponse(FileService.DownloadableFile downloadableFile) {
        MediaType mediaType = StringUtils.hasText(downloadableFile.contentType())
                ? MediaType.parseMediaType(downloadableFile.contentType())
                : MediaType.APPLICATION_OCTET_STREAM;
        ContentDisposition disposition = downloadableFile.attachment()
                ? ContentDisposition.attachment()
                .filename(downloadableFile.originalFilename(), StandardCharsets.UTF_8)
                .build()
                : ContentDisposition.inline()
                .filename(downloadableFile.originalFilename(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(downloadableFile.fileSize() == null ? -1 : downloadableFile.fileSize())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(downloadableFile.resource());
    }
}
