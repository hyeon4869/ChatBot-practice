package gpt.domain;

import gpt.domain.*;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

//<<< PoEAA / Repository
@RepositoryRestResource(collectionResourceRel = "chatBots", path = "chatBots")
public interface ChatBotRepository
    extends PagingAndSortingRepository<ChatBot, Long> {}
