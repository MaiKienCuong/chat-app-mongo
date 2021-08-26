package iuh.dhktpm14.cnm.chatappmongo.service;

import iuh.dhktpm14.cnm.chatappmongo.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

}
