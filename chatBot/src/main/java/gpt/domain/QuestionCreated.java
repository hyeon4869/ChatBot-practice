package gpt.domain;

import gpt.domain.*;
import gpt.infra.AbstractEvent;
import java.time.LocalDate;
import java.util.*;
import lombok.*;

//<<< DDD / Domain Event
@Data
@ToString
public class QuestionCreated extends AbstractEvent {

    private Long id;
    private String question;
    private String answer;

    public QuestionCreated(ChatBot aggregate) {
        super(aggregate);
    }

    public QuestionCreated() {
        super();
    }
}
//>>> DDD / Domain Event
