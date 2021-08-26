package iuh.dhktpm14.cnm.chatappmongo.repository;

import iuh.dhktpm14.cnm.chatappmongo.entity.Inbox;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface InboxRepository extends MongoRepository<Inbox, String> {

    /**
     * lấy tất cả inbox của userId mà inbox đó không trống
     */
    @Query(value = "{ofUserId: ?0, empty: false}")
    Page<Inbox> findAllByOfUserId(String ofUserId, Pageable pageable);

    Inbox findByOfUserIdAndRoomId(String ofUserId, String roomId);

    boolean existsByIdAndOfUserId(String id, String ofUserId);

    Inbox findByIdAndOfUserId(String id, String ofUserId);

}
