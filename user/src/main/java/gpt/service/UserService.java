package gpt.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import gpt.config.RetryConfig.RetryExhaustedException;
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
    // //HttpSesrvletResponse, ServerHttpResponse 의 차이 
    // public Mono<User> kakaoLogin(String code, ServerHttpResponse response) {
    //             return getToken(code)
    //                 .doOnNext(token -> response.getHeaders().get(token.getAccess_token()))//반환타입이 void일 떄 적합하며 동기적, 주로 로그, 상태 업데이트, http헤더 설정할 때 사용
    //                 .map(token -> User.toEntity(token));
    //         };
    

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
                    .doOnError(error -> logger.error("Failed to request Kakao token", error)) // 에러 로그를 남기기
                    .onErrorResume(e -> {
                        if (e instanceof RetryExhaustedException) {
                            // 재시도 횟수 초과
                            return Mono.error(new IllegalStateException("네트워크 오류로 인해 카카오 로그인을 완료할 수 없습니다. 잠시 후에 다시 시도해주세요", e));
                        } else {
                            return Mono.error(new IllegalStateException("카카오 로그인 중 오류가 발생했습니다. 잠시 후에 다시 시도해주세요", e));
                        }
                    });
        });
    }
        public Mono<String> kakaoLogout(String access_token) {
        logger.info("카카오 로그아웃 요청 시작: access_token = {}", access_token);

        return Mono.defer(() -> {
            return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("kapi.kakao.com")
                        .path("/v1/user/logout")
                        .build())
                .header("Authorization", "Bearer " + access_token)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(response -> logger.info("카카오 로그아웃 성공: 응답 = {}", response))
                .doOnError(error -> logger.error("카카오 로그아웃 실패: 에러 = {}", error.getMessage()))
                .onErrorResume(e -> {
                    logger.error("로그아웃 도중 오류 발생", e);
                    return Mono.error(new IllegalStateException("로그아웃 도중 오류가 발생하였습니다.", e));
                });
        });
    }

}
