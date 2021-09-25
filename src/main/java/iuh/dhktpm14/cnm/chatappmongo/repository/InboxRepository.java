package iuh.dhktpm14.cnm.chatappmongo.repository;

import iuh.dhktpm14.cnm.chatappmongo.entity.Inbox;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface InboxRepository extends MongoRepository<Inbox, String> {

    /**
     * lấy tất cả inbox của userId mà inbox đó không trống
     */
    @Query(value = "{ofUserId: ?0, empty: false}", sort = "{lastTime: -1}")
    Page<Inbox> getAllInboxOfUser(String ofUserId, Pageable pageable);

    /**
     *
     */
    boolean existsByOfUserIdAndRoomId(String ofUserId, String roomId);

    /**
     * tìm inbox theo userId và roomId
     */
    Optional<Inbox> findByOfUserIdAndRoomId(String ofUserId, String roomId);

    /**
     * kiểm tra xem inboxId có phải thuộc về người này hay không
     */
    boolean existsByIdAndOfUserId(String id, String ofUserId);

    /**
     *
     */
    Optional<Inbox> findByIdAndOfUserId(String id, String ofUserId);

}
