package gpt.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import gpt.config.RetryConfig.RetryExhaustedException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Service
@Transactional(readOnly = true)
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final String BASE_URL = "https://kauth.kakao.com";
    private static final String CLIENT_ID = "81e0765db461031e29617a607d03d786"; // 실제 REST API 키로 대체하세요
    private static final String REDIRECT_URI = "http://localhost:8082/users/kakaoLogin"; // 실제 리디렉션 URI로 대체하세요

    @Autowired
    private WebClient webClient;

    @Autowired
    private Retry retryConfig;

    // @Autowired
    // private RetryConfig retryConfig;

    public Mono<String> kakaoAuth() {
        return Mono.defer(() -> {
            // defer를 사용하는 이유는 UUID와 같이 고유한 데이터를 생성하기 위함으로 매번 새로운 인스턴스를 생성함
            // 만약 사용하지 않는다면 state값을 재사용하여 생성하기에 고유 특성이 자라짐
            try {
                // CSRF 방지를 위한 state 파라미터 생성 (예: UUID)
                String state = UUID.randomUUID().toString();

                String authorizeUrl = String.format(
                        "%s/oauth/authorize?response_type=code&client_id=%s&redirect_uri=%s&state=%s",
                        BASE_URL, CLIENT_ID, REDIRECT_URI, state);

                logger.info("카카오 인증 URL: " + authorizeUrl);

                // 카카오 인증 URL을 반환합니다. 사용자는 이 URL을 통해 카카오 로그인 페이지로 리다이렉트 됩니다.
                return Mono.just(authorizeUrl);
            } catch (Exception e) {
                logger.error("카카오 인증 중 오류 발생", e);
                return Mono.error(new IllegalStateException("카카오 인증 중 오류가 발생했습니다. 잠시 후에 다시 시도해주세요", e));
                // defer를 사용했다면 mono.error로 예외처리하기

            }
        });
    }

    public Mono<String> kakaoLogin(String code) {
        return Mono.defer(() -> {
            
                String tokenUrl = BASE_URL + "/oauth/token";

                return webClient.post()
                        .uri(tokenUrl)
                        .bodyValue(String.format("grant_type=authorization_code&client_id=%s&redirect_uri=%s&code=%s",
                                CLIENT_ID, REDIRECT_URI, code))
                        .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                        .retrieve()
                        .bodyToMono(String.class)
                        .retryWhen(retryConfig)
                        .doOnSuccess(token -> logger.info("Kakao token: " + token))
                        .doOnError(error -> logger.error("Failed to request Kakao token", error))//에러 로그를 남기기
                        .onErrorResume(e -> {
                            if(e instanceof RetryExhaustedException){//에러 발생 후 추후 실행할 로직 
                                //재시도 횟수 초과
                                return Mono.error(new IllegalStateException("네트워크 오류로 인해 카카오 로그인을 완료할 수 없습니다. 잠시 후에 다시 시도해주세요", e));
                
                            } else {
                                return Mono.error(new IllegalStateException("카카오 로그인 중 오류가 발생했습니다. 잠시 후에 다시 시도해주세요", e));
                            }
                        });
        });
    }

}
