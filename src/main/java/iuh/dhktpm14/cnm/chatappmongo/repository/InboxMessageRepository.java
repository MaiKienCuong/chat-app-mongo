package iuh.dhktpm14.cnm.chatappmongo.repository;

import iuh.dhktpm14.cnm.chatappmongo.entity.InboxMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface InboxMessageRepository extends MongoRepository<InboxMessage, String> {

    /**
     * lấy ra tất cả InboxMessage theo inboxId, mục đích là để lấy ra danh sách messageId của inboxId này
     * sau đó lấy ra tất cả message trong collection message mà có id nằm trong danh sách id vừa tìm ở trên
     * hàm này không được gọi trực tiếp mà được gọi bởi hàm khác
     */
    @Query(value = "{inboxId: ?0}", sort = "{'messageCreateAt': -1}")
    List<InboxMessage> findAllByInboxId(String inboxId, Pageable pageable);

    void deleteByInboxId(String inboxId);

    boolean existsByInboxIdAndMessageId(String inboxId, String messageId);

}
