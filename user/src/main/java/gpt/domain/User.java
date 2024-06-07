package gpt.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PostPersist;
import javax.persistence.PreRemove;
import javax.persistence.Table;

import gpt.UserApplication;
import gpt.dto.DeletedId;
import gpt.dto.FoundId;
import gpt.dto.FoundPassword;
import gpt.dto.LoggedIn;
import gpt.dto.ResponseToken;
import gpt.dto.SignedUp;
import gpt.dto.UpdatedInformation;
import gpt.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "User_table")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// <<< DDD / Aggregate Root
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String userId;

    private String password;

    private String nickName;

    private String email;

    private String token_type;

    private String access_token;

    private String id_token;

    private String refresh_token;

    private String scope;

    private int expires_in;

    private int refresh_token_expires_in;

    @PostPersist
    public void onPostPersist() {
        SignedUp signedUp = new SignedUp(this);
        signedUp.publishAfterCommit();

        LoggedIn loggedIn = new LoggedIn(this);
        loggedIn.publishAfterCommit();

        FoundId foundId = new FoundId(this);
        foundId.publishAfterCommit();

        FoundPassword foundPassword = new FoundPassword(this);
        foundPassword.publishAfterCommit();

        DeletedId deletedId = new DeletedId(this);
        deletedId.publishAfterCommit();

        UpdatedInformation updatedInformation = new UpdatedInformation(this);
        updatedInformation.publishAfterCommit();

        ResponseToken responseToken = new ResponseToken(this);
        responseToken.publishAfterCommit();

    }

    @PreRemove
    public void onPreRemove() {
    }

    public static UserRepository repository() {
        UserRepository userRepository = UserApplication.applicationContext.getBean(
                UserRepository.class);
        return userRepository;
    }

    public static User toEntity(ResponseToken responseToken) {
        
        System.out.println("db에 저장 중");
        
        User user = User.builder()
                .token_type(responseToken.getToken_type())
                .access_token(responseToken.getAccess_token())
                // .id_token(responseToken.getId_token())
                .refresh_token(responseToken.getRefresh_token())
                .scope(responseToken.getScope())
                .expires_in(responseToken.getExpires_in())
                .refresh_token_expires_in(responseToken.getRefresh_token_expires_in())
                .build();

        repository().save(user);
        
        return user;
    }
}
// >>> DDD / Aggregate Root
