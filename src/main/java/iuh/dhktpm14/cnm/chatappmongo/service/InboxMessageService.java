package iuh.dhktpm14.cnm.chatappmongo.service;

import iuh.dhktpm14.cnm.chatappmongo.entity.Inbox;
import iuh.dhktpm14.cnm.chatappmongo.entity.InboxMessage;
import iuh.dhktpm14.cnm.chatappmongo.repository.InboxMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class InboxMessageService {

    @Autowired
    private InboxMessageRepository inboxMessageRepository;

    @Autowired
    private InboxService inboxService;

    private static final Logger logger = Logger.getLogger(InboxMessageService.class.getName());

    /**
     * lấy ra tất cả InboxMessage theo inboxId, mục đích là để lấy ra danh sách messageId của inboxId này
     * sau đó lấy ra tất cả message trong collection message mà có id nằm trong danh sách id vừa tìm ở trên
     * hàm này không được gọi trực tiếp mà được gọi bởi hàm khác
     */
    public Page<InboxMessage> getAllInboxMessageOfInbox(String inboxId, Pageable pageable) {
        return inboxMessageRepository.getAllInboxMessageOfInbox(inboxId, pageable);
    }

    /**
     * xóa inbox theo inboxId
     */
    public void deleteAllMessageOfInbox(String inboxId) {
        inboxMessageRepository.deleteAllMessageOfInbox(inboxId);
    }

    /**
     * kiểm tra xem tin nhắn này có thuộc inboxId hay không
     */
    public boolean existsByInboxIdAndMessageId(String inboxId, String messageId) {
        return inboxMessageRepository.existsByInboxIdAndMessageId(inboxId, messageId);
    }

    /*
    lấy tin nhắn cuối cùng của inbox
     */
    public Optional<InboxMessage> getLastMessageByInbox(String inboxId) {
        return inboxMessageRepository.getLastMessageByInbox(inboxId);
    }

    public InboxMessage save(InboxMessage inboxMessage) {
        return inboxMessageRepository.save(inboxMessage);
    }

    public void deleteAllMessageOfUserInRoom(String ofUserId, String roomId) {
        Optional<Inbox> inboxOptional = inboxService.findByOfUserIdAndRoomId(ofUserId, roomId);
        if (inboxOptional.isPresent()) {
            var inbox = inboxOptional.get();

            logger.log(Level.INFO, "delete all message of inboxId = {0} in roomId = {1}",
                    new Object[]{ inbox.getId(), roomId });
            deleteAllMessageOfInbox(inbox.getId());
        }
    }

}
