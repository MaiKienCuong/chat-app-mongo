package iuh.dhktpm14.cnm.chatappmongo.repository;

import iuh.dhktpm14.cnm.chatappmongo.entity.InboxMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InboxMessageRepository extends MongoRepository<InboxMessage, String> {

    /**
     * lấy ra tất cả InboxMessage theo inboxId, mục đích là để lấy ra danh sách messageId của inboxId này
     * sau đó lấy ra tất cả message trong collection message mà có id nằm trong danh sách id vừa tìm ở trên
     * hàm này không được gọi trực tiếp mà được gọi bởi hàm khác
     */
    @Query(value = "{inboxId: ?0}", sort = "{'messageCreateAt': -1}")
    Page<InboxMessage> getAllInboxMessageOfInbox(String inboxId, Pageable pageable);

    /**
     * xóa inbox theo inboxId
     */
    @Query(value = "{inboxId: ?0}", delete = true)
    void deleteAllMessageOfInbox(String inboxId);

    /**
     * kiểm tra xem tin nhắn này có thuộc inboxId hay không
     */
    boolean existsByInboxIdAndMessageId(String inboxId, String messageId);

    /*
    lấy tin nhắn cuối cùng của inbox
     */
    @Aggregation(pipeline = {
            "{$match: {inboxId: ?0}}",
            "{$sort: {messageCreateAt: -1}}",
            "{$limit: 1}" })
    Optional<InboxMessage> getLastMessageByInbox(String inboxId);

    long deleteAllByInboxIdIn(List<String> inboxIds);

}
