package gpt.dto;

public class KakaoTokenResponse {
    private String tokenType;
    private String accessToken;
    private String idToken; // 추가된 필드
    private Integer expiresIn;
    private String refreshToken;
    private Integer refreshTokenExpiresIn;
    private String scope;
    
    // Default constructor, getters and setters omitted for brevity
}
