package com.ieum.ansimdonghaeng.domain.auth.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ieum.ansimdonghaeng.common.exception.CustomException;
import com.ieum.ansimdonghaeng.common.exception.ErrorCode;
import com.ieum.ansimdonghaeng.domain.auth.dto.response.KakaoUserInfo;
import io.netty.channel.ChannelOption;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

@Component
@RequiredArgsConstructor
public class KakaoOAuthWebClient implements KakaoOAuthClient {

    private final WebClient.Builder webClientBuilder;
    private final KakaoOAuthProperties kakaoOAuthProperties;

    @Override
    public String getAccessToken(String authorizationCode) {
        try {
            BodyInserters.FormInserter<String> formData = BodyInserters.fromFormData("grant_type", "authorization_code")
                    .with("client_id", kakaoOAuthProperties.getRestApiKey())
                    .with("redirect_uri", kakaoOAuthProperties.getRedirectUri())
                    .with("code", authorizationCode);

            if (StringUtils.hasText(kakaoOAuthProperties.getClientSecret())) {
                formData.with("client_secret", kakaoOAuthProperties.getClientSecret());
            }

            KakaoTokenResponse response = kakaoWebClient()
                    .post()
                    .uri(kakaoOAuthProperties.getTokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                            clientResponse.createException()
                                    .map(exception -> new CustomException(
                                            ErrorCode.OAUTH_PROVIDER_ERROR,
                                            "Kakao OAuth rejected the authorization code."
                                    )))
                    .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                            clientResponse.createException()
                                    .map(exception -> new CustomException(
                                            ErrorCode.OAUTH_PROVIDER_ERROR,
                                            "Kakao OAuth provider is temporarily unavailable."
                                    )))
                    .bodyToMono(KakaoTokenResponse.class)
                    .timeout(Duration.ofMillis(kakaoOAuthProperties.getResponseTimeoutMillis()))
                    .block();

            if (response == null || response.accessToken() == null || response.accessToken().isBlank()) {
                throw new CustomException(ErrorCode.OAUTH_PROVIDER_ERROR, "Kakao access token was not returned.");
            }

            return response.accessToken();
        } catch (CustomException exception) {
            throw exception;
        } catch (WebClientRequestException exception) {
            throw new CustomException(ErrorCode.OAUTH_PROVIDER_ERROR, "Failed to connect to Kakao OAuth provider.");
        } catch (WebClientResponseException exception) {
            throw new CustomException(ErrorCode.OAUTH_PROVIDER_ERROR, "Failed to retrieve Kakao access token.");
        }
    }

    @Override
    public KakaoUserInfo getUserInfo(String accessToken) {
        try {
            KakaoUserResponse response = kakaoWebClient()
                    .get()
                    .uri(kakaoOAuthProperties.getUserInfoUri())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                            clientResponse.createException()
                                    .map(exception -> new CustomException(
                                            ErrorCode.OAUTH_PROVIDER_ERROR,
                                            "Kakao OAuth rejected the access token."
                                    )))
                    .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                            clientResponse.createException()
                                    .map(exception -> new CustomException(
                                            ErrorCode.OAUTH_PROVIDER_ERROR,
                                            "Kakao OAuth provider is temporarily unavailable."
                                    )))
                    .bodyToMono(KakaoUserResponse.class)
                    .timeout(Duration.ofMillis(kakaoOAuthProperties.getResponseTimeoutMillis()))
                    .block();

            return toUserInfo(response);
        } catch (CustomException exception) {
            throw exception;
        } catch (WebClientRequestException exception) {
            throw new CustomException(ErrorCode.OAUTH_PROVIDER_ERROR, "Failed to connect to Kakao OAuth provider.");
        } catch (WebClientResponseException exception) {
            throw new CustomException(ErrorCode.OAUTH_PROVIDER_ERROR, "Failed to retrieve Kakao user information.");
        }
    }

    private KakaoUserInfo toUserInfo(KakaoUserResponse response) {
        if (response == null || response.id() == null) {
            throw new CustomException(ErrorCode.OAUTH_PROVIDER_ERROR, "Kakao user id was not returned.");
        }

        String email = Optional.ofNullable(response.kakaoAccount())
                .map(KakaoAccount::email)
                .orElseThrow(() -> new CustomException(ErrorCode.OAUTH_EMAIL_NOT_PROVIDED));

        String nickname = Optional.ofNullable(response.kakaoAccount())
                .map(KakaoAccount::profile)
                .map(KakaoProfile::nickname)
                .filter(value -> !value.isBlank())
                .orElse(email);

        return new KakaoUserInfo(String.valueOf(response.id()), email, nickname);
    }

    private WebClient kakaoWebClient() {
        return webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, kakaoOAuthProperties.getConnectTimeoutMillis())
                        .responseTimeout(Duration.ofMillis(kakaoOAuthProperties.getResponseTimeoutMillis()))))
                .build();
    }

    private record KakaoTokenResponse(
            @JsonProperty("access_token")
            String accessToken
    ) {
    }

    private record KakaoUserResponse(
            Long id,
            @JsonProperty("kakao_account")
            KakaoAccount kakaoAccount
    ) {
    }

    private record KakaoAccount(
            String email,
            KakaoProfile profile
    ) {
    }

    private record KakaoProfile(
            String nickname
    ) {
    }
}
