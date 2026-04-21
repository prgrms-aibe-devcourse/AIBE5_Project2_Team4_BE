package com.ieum.ansimdonghaeng.domain.auth.service;

import com.ieum.ansimdonghaeng.common.exception.CustomException;
import com.ieum.ansimdonghaeng.common.exception.ErrorCode;
import com.ieum.ansimdonghaeng.domain.auth.dto.request.ForgotPasswordRequest;
import com.ieum.ansimdonghaeng.domain.auth.dto.request.ResetPasswordRequest;
import com.ieum.ansimdonghaeng.domain.auth.mail.PasswordResetEmailSender;
import com.ieum.ansimdonghaeng.domain.auth.repository.RefreshTokenRepository;
import com.ieum.ansimdonghaeng.domain.auth.store.PasswordResetTokenStore;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenStore tokenStore;
    private final PasswordResetEmailSender emailSender;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.password-reset.token-expiration-minutes:10}")
    private int tokenExpirationMinutes;

    @Value("${app.password-reset.reset-url-base:http://localhost:5173/reset-password}")
    private String resetUrlBase;

    public void sendResetEmail(ForgotPasswordRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
            String token = tokenStore.createToken(email, tokenExpirationMinutes);
            String resetLink = resetUrlBase + "?token=" + token;
            emailSender.sendPasswordResetEmail(email, resetLink);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String email = tokenStore.consumeValidToken(request.resetToken())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_RESET_TOKEN));

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_RESET_TOKEN));

        user.updatePasswordHash(passwordEncoder.encode(request.newPassword()));
        LocalDateTime revokedAt = LocalDateTime.now();
        refreshTokenRepository.findAllByUser_IdAndActiveYnTrue(user.getId())
                .forEach(refreshToken -> refreshToken.revoke(revokedAt));
    }
}
