package gpt.infra;

import gpt.domain.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

@Component
public class ChatBotHateoasProcessor
    implements RepresentationModelProcessor<EntityModel<ChatBot>> {

    @Override
    public EntityModel<ChatBot> process(EntityModel<ChatBot> model) {
        return model;
    }
}
