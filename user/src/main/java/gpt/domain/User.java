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
import gpt.dto.SignedUp;
import gpt.dto.UpdatedInformation;
import gpt.repository.UserRepository;
import lombok.Data;

@Entity
@Table(name = "User_table")
@Data
//<<< DDD / Aggregate Root
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String userId;

    private String password;

    private String nickName;

    private String email;

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
    }

    @PreRemove
    public void onPreRemove() {}

    public static UserRepository repository() {
        UserRepository userRepository = UserApplication.applicationContext.getBean(
            UserRepository.class
        );
        return userRepository;
    }
}
//>>> DDD / Aggregate Root
