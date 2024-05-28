package gpt.controller;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gpt.service.UserService;
import reactor.core.publisher.Mono;

//<<< Clean Arch / Inbound Adaptor

@RestController
@RequestMapping(value = "/users")
@Transactional
public class UserController {

    @Autowired
    private UserService userService;

    // @GetMapping("/kakaoAuth")
    // public Mono<ResponseEntity<?>> kakaoAuth() {
    //     return userService.kakaoAuth()
    //             .map(url -> ResponseEntity.ok().body(url)); // 카카오 인증 URL을 클라이언트에게 전달
    // }

    @GetMapping("/kakaoAuth")
    public Mono<ResponseEntity<?>> kakaoAuth() {
        return userService.kakaoAuth()
            .map(url -> ResponseEntity.ok(url));
            
    }

    @GetMapping("/kakaoLogin")
    public Mono<String> kakaoLogin(@RequestParam String code) {
        return userService.kakaoLogin(code);
    }

  
}
// >>> Clean Arch / Inbound Adaptor