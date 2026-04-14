package com.ieum.ansimdonghaeng.common.websocket;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.websocket")
public class WebSocketProperties {

    @NotBlank
    private String endpoint;

    private List<String> applicationDestinationPrefixes = List.of("/app");

    private List<String> simpleBrokerDestinations = List.of("/topic", "/queue");
}
