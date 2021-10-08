package iuh.dhktpm14.cnm.chatappmongo.service;

import iuh.dhktpm14.cnm.chatappmongo.entity.Inbox;
import iuh.dhktpm14.cnm.chatappmongo.entity.InboxMessage;
import iuh.dhktpm14.cnm.chatappmongo.entity.Message;
import iuh.dhktpm14.cnm.chatappmongo.entity.Reaction;
import iuh.dhktpm14.cnm.chatappmongo.entity.Room;
import iuh.dhktpm14.cnm.chatappmongo.repository.InboxMessageRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.InboxRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.MessageRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private static final Logger logger = Logger.getLogger(MessageService.class.getName());

    /*
    gỡ một tin nhắn
     */
    public void deleteMessage(String messageId, String newContent) {
        logger.log(Level.INFO, "deleting messageId = {0}, new content = {1}",
                new Object[]{ messageId, newContent });
        var criteria = Criteria.where("_id").is(messageId);
        var update = new Update();
        update.set("content", newContent);
        update.set("deleted", true);
        mongoTemplate.updateFirst(Query.query(criteria), update, Message.class);
    }

    /*
    thêm cảm xúc vào tin nhắn
     */
    public void addReactToMessage(String messageId, Reaction reaction) {
        logger.log(Level.INFO, "adding reaction = {0}, to messageId = {1}",
                new Object[]{ reaction, messageId });
        var criteria = Criteria.where("_id").is(messageId);
        var update = new Update();
        update.push("reactions", reaction);
        mongoTemplate.updateFirst(Query.query(criteria), update, Message.class);
    }

    /*
    kiểm tra tin nhắn có thuộc quyền của user hay không, user có quyền xem tin nhắn hay không
     */
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

    public Optional<Message> getLastMessageOfRoom(String userId, String roomId) {
        Optional<Inbox> inboxOptional = inboxRepository.findByOfUserIdAndRoomId(userId, roomId);
        if (inboxOptional.isPresent()) {
            var inbox = inboxOptional.get();
            Optional<InboxMessage> inboxMessageOptional = inboxMessageRepository.getLastMessageByInbox(inbox.getId());
            if (inboxMessageOptional.isPresent()) {
                var inboxMessage = inboxMessageOptional.get();
                if (inboxMessage.getMessageId() != null)
                    return messageRepository.findById(inboxMessage.getMessageId());
            }
        }
        return Optional.empty();
    }

    /**
     * lấy tất cả tin nhắn của inbox, khi gọi sẽ truyền messageIds=inbox.getMessageIds()
     */
    public Page<Message> findAllByIdInMessageIdsPaged(List<String> messageIds, Pageable pageable) {
        return messageRepository.findAllByIdInMessageIdsPaged(messageIds, pageable);
    }

    public List<Message> findAllByIdInMessageIds(List<String> messageIds, Pageable pageable) {
        return messageRepository.findAllByIdInMessageIds(messageIds, pageable);
    }

    public Message save(Message message) {
        return messageRepository.save(message);
    }

    public Optional<Message> findById(String messageId) {
        return messageRepository.findById(messageId);
    }

}
