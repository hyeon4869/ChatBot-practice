package gpt.dto;

import gpt.domain.User;
import gpt.infra.AbstractEvent;
import lombok.Data;
import lombok.ToString;

//<<< DDD / Domain Event
@Data
@ToString
public class FoundId extends AbstractEvent {

    private Long id;
    private String userId;
    private String password;
    private String nickName;
    private String email;

    public FoundId(User aggregate) {
        super(aggregate);
    }

    public FoundId() {
        super();
    }
}
//>>> DDD / Domain Event
