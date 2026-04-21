package com.ieum.ansimdonghaeng.domain.auth.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "mail.enabled", havingValue = "false", matchIfMissing = true)
public class ConsoleMailService implements MailService {

    @Override
    public void sendPasswordResetEmail(String to, String resetLink) {
        log.info("[PASSWORD RESET EMAIL] to={} link={}", to, resetLink);
    }
}