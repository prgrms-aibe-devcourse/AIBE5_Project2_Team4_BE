package com.ieum.ansimdonghaeng.common.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.file-storage")
public class FileStorageProperties {

    @NotBlank
    private String baseDir;
}
