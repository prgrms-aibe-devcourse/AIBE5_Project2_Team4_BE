package com.ieum.ansimdonghaeng.common.websocket;

import com.ieum.ansimdonghaeng.common.jwt.JwtTokenProvider;
import com.ieum.ansimdonghaeng.common.security.CustomUserDetailsService;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            Authentication authentication = authenticate(accessor.getNativeHeader("Authorization"));
            accessor.setUser(authentication);
        }

        return message;
    }

    private Authentication authenticate(List<String> authorizationHeaders) {
        String bearerToken = authorizationHeaders == null || authorizationHeaders.isEmpty()
                ? null
                : authorizationHeaders.getFirst();

        if (!StringUtils.hasText(bearerToken) || !bearerToken.startsWith("Bearer ")) {
            throw new IllegalArgumentException("WebSocket Authorization header is required.");
        }

        String token = bearerToken.substring("Bearer ".length());
        if (!jwtTokenProvider.validateToken(token) || !jwtTokenProvider.isAccessToken(token)) {
            throw new IllegalArgumentException("Invalid WebSocket access token.");
        }

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(jwtTokenProvider.getUsername(token));
        Principal principal = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        return (Authentication) principal;
    }
}
