package gpt.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final String BASE_URL = "https://kauth.kakao.com";
    private static final String CLIENT_ID = "81e0765db461031e29617a607d03d786"; // 실제 REST API 키로 대체하세요
    private static final String REDIRECT_URI = "http://localhost:8082/users/kakaoLogin"; // 실제 리디렉션 URI로 대체하세요
    private static final String LOGOUT_RE_URI = "http://localhost:8082/users/kakaoLogout";
    @Autowired
    private WebClient webClient;

    public Mono<String> kakaoAuth() {
        String authorizeUrl = String.format("%s/oauth/authorize?response_type=code&client_id=%s&redirect_uri=%s",
                BASE_URL, CLIENT_ID, REDIRECT_URI);

        logger.info("카카오 인증 URL: " + authorizeUrl);

        // 카카오 인증 URL을 반환합니다. 사용자는 이 URL을 통해 카카오 로그인 페이지로 리다이렉트 됩니다.
        return Mono.just(authorizeUrl);
    }

    public Mono<String> kakaoLogin(String code) {

        String tokenUrl = BASE_URL + "/oauth/token";

        return webClient.post()
                .uri(tokenUrl)
                .bodyValue(String.format("grant_type=authorization_code&client_id=%s&redirect_uri=%s&code=%s",
                        CLIENT_ID, REDIRECT_URI, code))
                .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(token -> logger.info("Kakao token: " + token))
                .doOnError(error -> logger.error("Failed to request Kakao token", error));

    }

    public Mono<String> kakaoLogout(String access_token) {
  
        return Mono.defer(() -> {
            return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("kauth.kakao.com")
                        .path("/oauth/logout")
                        .queryParam("client_id", CLIENT_ID)
                        .queryParam("logout_redirect_uri", LOGOUT_RE_URI)
                        .build())
                .header("Authorization", "Bearer " + access_token)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    // 로그아웃 실패 시 처리
                    return Mono.error(new IllegalStateException("로그아웃 도중 오류가 발생하였습니다."));
                });
        });
    }
}
