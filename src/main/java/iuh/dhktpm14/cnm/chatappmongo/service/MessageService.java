package iuh.dhktpm14.cnm.chatappmongo.service;

import iuh.dhktpm14.cnm.chatappmongo.entity.Inbox;
import iuh.dhktpm14.cnm.chatappmongo.entity.Message;
import iuh.dhktpm14.cnm.chatappmongo.entity.Reaction;
import iuh.dhktpm14.cnm.chatappmongo.entity.Room;
import iuh.dhktpm14.cnm.chatappmongo.repository.InboxMessageRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.InboxRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.MessageRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MessageService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private InboxRepository inboxRepository;

    @Autowired
    private InboxMessageRepository inboxMessageRepository;

    public void deleteMessage(String messageId, String newContent) {
        var criteria = Criteria.where("_id").is(messageId);
        var update = new Update();
        update.set("content", newContent);
        update.set("deleted", true);
        mongoTemplate.updateFirst(Query.query(criteria), update, Message.class);
    }

    public void addReactToMessage(String messageId, Reaction reaction) {
        var criteria = Criteria.where("_id").is(messageId);
        var update = new Update();
        update.push("reactions", reaction);
        mongoTemplate.updateFirst(Query.query(criteria), update, Message.class);
    }

    public boolean checkPermissionToSeeMessage(String messageId, String userId) {
        Optional<Message> messageOptional = messageRepository.findById(messageId);
        if (messageOptional.isEmpty())
            return false;
        var message = messageOptional.get();
        Optional<Room> roomOptional = roomRepository.findById(message.getRoomId());
        if (roomOptional.isEmpty())
            return false;
        var room = roomOptional.get();
        if (! roomRepository.isMemberOfRoom(userId, room.getId()))
            return false;
        Optional<Inbox> inboxOptional = inboxRepository.findByOfUserIdAndRoomId(userId, room.getId());
        if (inboxOptional.isEmpty())
            return false;
        var inbox = inboxOptional.get();
        if (inbox.isEmpty())
            return false;
        return inboxMessageRepository.existsByInboxIdAndMessageId(inbox.getId(), messageId);
    }

}
