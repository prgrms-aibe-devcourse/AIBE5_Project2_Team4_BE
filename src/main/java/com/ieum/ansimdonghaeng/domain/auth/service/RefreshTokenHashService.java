package com.ieum.ansimdonghaeng.domain.auth.service;

import com.ieum.ansimdonghaeng.common.jwt.JwtProperties;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenHashService {

    private static final String ALGORITHM = "HmacSHA256";
    private static final String HASH_PREFIX = "hmac-sha256:";

    private final JwtProperties jwtProperties;

    public String hash(String refreshTokenValue) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8), ALGORITHM));
            byte[] digest = mac.doFinal(refreshTokenValue.getBytes(StandardCharsets.UTF_8));
            return HASH_PREFIX + HexFormat.of().formatHex(digest);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to hash refresh token.", exception);
        }
    }
}
