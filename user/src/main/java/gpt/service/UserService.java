package gpt.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import gpt.config.RetryConfig.RetryExhaustedException;
import gpt.domain.User;
import gpt.dto.ResponseToken;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Service
@Transactional(readOnly = true)
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final String BASE_URL = "https://kauth.kakao.com";
    private static final String CLIENT_ID = "81e0765db461031e29617a607d03d786"; // 실제 REST API 키로 대체하세요
    private static final String REDIRECT_URI = "http://localhost:8082/users/kakaoLogin"; // 실제 리디렉션 URI로 대체하세요
    private static final String LOGOUT_RE_URI = "http://localhost:8082/users/kakaoLogout";
    @Autowired
    private WebClient webClient;

    @Autowired
    private Retry retryConfig;

    // @Autowired
    // private RetryConfig retryConfig;

    //카카오톡 인증
    public Mono<String> kakaoAuth() {
        return Mono.defer(() -> Mono.fromCallable(() -> {

            // CSRF 방지를 위한 state 파라미터 생성 (예: UUID)
            String state = UUID.randomUUID().toString();

            String authorizeUrl = String.format(
                    "%s/oauth/authorize?response_type=code&client_id=%s&redirect_uri=%s&state=%s",
                    BASE_URL, CLIENT_ID, REDIRECT_URI, state);

            // 보안상의 이유로 debug 레벨로 로깅을 변경
            logger.info("카카오 인증 URL: " + authorizeUrl);

            // 카카오 인증 URL을 반환. 사용자는 이 URL을 통해 카카오 로그인 페이지로 리다이렉트 됨.
            return authorizeUrl;
            // defer를 사용했기 때문에, 여기서 발생하는 예외는 자동으로 Mono.error로 변환됩니다.
        }).onErrorResume(e -> {
            logger.error("카카오 인증 중 오류 발생", e);
            return Mono.error(new IllegalStateException("카카오 인증 중 오류가 발생했습니다. 잠시 후에 다시 시도해주세요", e));
        }));
    }

    // //카카오톡 로그인 
    public Mono<User> kakaoLogin(String code){
        return getToken(code)
            .map(User :: toEntity);//User 클래스의 toEntity를 실행, db저장은 동기적으로 이루어지기 때문에 map을 사용
    }

    //토큰 얻기
    public Mono<ResponseToken> getToken(String code) {
        return Mono.defer(() -> {

            String tokenUrl = BASE_URL + "/oauth/token";

            return webClient.post()
                    .uri(tokenUrl)
                    .bodyValue(String.format("grant_type=authorization_code&client_id=%s&redirect_uri=%s&code=%s",
                            CLIENT_ID, REDIRECT_URI, code))
                    .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                    .retrieve()
                    .bodyToMono(ResponseToken.class)
                    .retryWhen(retryConfig)
                    .doOnSuccess(token -> logger.info("Kakao token: " + token))
                    .doOnError(error -> logger.error("Failed to request Kakao token", error))// 에러 로그를 남기기
                    .onErrorResume(e -> {
                        if (e instanceof RetryExhaustedException) {// 에러 발생 후 추후 실행할 로직
                            // 재시도 횟수 초과
                            return Mono.error(
                                    new IllegalStateException("네트워크 오류로 인해 카카오 로그인을 완료할 수 없습니다. 잠시 후에 다시 시도해주세요", e));

                        } else {
                            return Mono.error(new IllegalStateException("카카오 로그인 중 오류가 발생했습니다. 잠시 후에 다시 시도해주세요", e));
                        }
                    });
        });
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
