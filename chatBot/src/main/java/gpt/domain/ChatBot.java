package gpt.domain;

import gpt.ChatBotApplication;
import gpt.domain.AnswerRepied;
import gpt.domain.QuestionCreated;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import lombok.Data;

@Entity
@Table(name = "ChatBot_table")
@Data
//<<< DDD / Aggregate Root
public class ChatBot {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String question;

    private String answer;

    @PostPersist
    public void onPostPersist() {
        QuestionCreated questionCreated = new QuestionCreated(this);
        questionCreated.publishAfterCommit();

        AnswerRepied answerRepied = new AnswerRepied(this);
        answerRepied.publishAfterCommit();
    }

    public static ChatBotRepository repository() {
        ChatBotRepository chatBotRepository = ChatBotApplication.applicationContext.getBean(
            ChatBotRepository.class
        );
        return chatBotRepository;
    }
}
//>>> DDD / Aggregate Root
