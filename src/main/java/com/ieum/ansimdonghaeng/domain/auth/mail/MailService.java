package com.ieum.ansimdonghaeng.domain.auth.mail;

public interface MailService {
    void sendPasswordResetEmail(String to, String resetLink);
}