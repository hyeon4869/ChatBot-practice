package gpt.controller;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gpt.domain.User;
import gpt.service.UserService;
import reactor.core.publisher.Mono;

//<<< Clean Arch / Inbound Adaptor

@RestController
@RequestMapping(value = "/users")
@Transactional
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/kakaoAuth")
    public Mono<ResponseEntity<String>> kakaoAuth() {
        return userService.kakaoAuth()
                .map(url -> ResponseEntity.ok().body(url)); // 카카오 인증 URL을 클라이언트에게 전달

    }

    @GetMapping("/kakaoLogin")
    public Mono<ResponseEntity<User>> kakaoLogin(@RequestParam String code) {
        return userService.getToken(code)
            .map(token -> {
                User user = User.toEntity(token);
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(token.getAccess_token());
                return ResponseEntity.ok().headers(headers).body(user);
            });
    }


    @GetMapping("/kakaoLogout")
    public Mono<String> kakaoLogout(@RequestHeader("Authorization") String accessToken) {//헤더의 값을 읽기 때문에 requestHeader 사용
        System.out.println("여기서부터 시작");
        System.out.println(accessToken);
        return userService.kakaoLogout(accessToken);
    }

}
// >>> Clean Arch / Inbound Adaptor
