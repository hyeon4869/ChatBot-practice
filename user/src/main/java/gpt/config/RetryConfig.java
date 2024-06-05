package gpt.config;

import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import io.netty.channel.ConnectTimeoutException;
import reactor.util.retry.Retry;

@Configuration
public class RetryConfig {

    // 재시도 정책을 설정하는 메서드
    @Bean
    public Retry createRetrySpec() {
        return Retry.backoff(3, Duration.ofSeconds(3))
                .maxBackoff(Duration.ofSeconds(10))
                .jitter(0.1)
                .filter(RetryConfig::isNetworkException) // 네트워크 오류에 대해서만 재시도합니다.
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> new RetryExhaustedException("재시도 횟수 초과"));
    }

    private static boolean isNetworkException(Throwable throwable) {
        return throwable instanceof WebClientResponseException ||
                throwable instanceof ConnectTimeoutException ||
                throwable instanceof UnknownHostException ||
                throwable instanceof SocketTimeoutException ||
                throwable instanceof NoRouteToHostException ||
                throwable instanceof SocketException; // 여기에 추가된 예외들
    }

    // 재시도 횟수 초과 시 발생하는 예외를 정의합니다.
    public static class RetryExhaustedException extends RuntimeException {
        public RetryExhaustedException(String message) {
            super(message);
        }
    }

}
