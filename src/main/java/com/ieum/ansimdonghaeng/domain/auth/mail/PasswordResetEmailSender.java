package com.ieum.ansimdonghaeng.domain.auth.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetEmailSender {

    private final MailService mailService;

    @Async
    public void sendPasswordResetEmail(String to, String resetLink) {
        try {
            mailService.sendPasswordResetEmail(to, resetLink);
        } catch (RuntimeException e) {
            log.warn("Failed to send password reset email. to={}", to, e);
        }
    }
}
