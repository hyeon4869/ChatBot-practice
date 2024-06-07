package gpt.dto;

import gpt.domain.User;
import gpt.infra.AbstractEvent;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ResponseToken extends AbstractEvent{

    private String token_type;

    private String access_token;

    private String id_token;

    private String refresh_token;

    private String scope;   

    private int expires_in;

    private int refresh_token_expires_in;
 
        public ResponseToken(User aggregate) {
        super(aggregate);
    }

    public ResponseToken() {
        super();
    }
}
