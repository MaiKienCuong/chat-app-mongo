package iuh.dhktpm14.cnm.chatappmongo.repository;

import iuh.dhktpm14.cnm.chatappmongo.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {

    /**
     * dùng để lấy tin nhắn cuối cùng của roomId
     */
    @Aggregation(pipeline = {
            "{$match: {roomId: ?0}}",
            "{$sort: {createAt: -1}}",
            "{$limit: 1}" })
    Message getLastMessageOfRoom(String roomId);

    /**
     * lấy tất cả tin nhắn của inbox, khi gọi sẽ truyền messageIds=inbox.getMessageIds()
     */
    @Query(value = "{id: {$in: ?0}}", sort = "{createAt: -1}")
    Page<Message> findAllByIdInMessageIdsPaged(List<String> messageIds, Pageable pageable);

    @Query(value = "{id: {$in: ?0}}", sort = "{createAt: -1}")
    List<Message> findAllByIdInMessageIds(List<String> messageIds, Pageable pageable);

    /**
     * đếm số tin nhắn mới của roomid
     * điều kiện là senderId của message khác với userId hiện tại
     * và userId hiện tại không có trong danh sách đã xem của tin nhắn
     */
    @Query(value = "{roomId: ?0, 'readByes.readByUserId': {$ne: ?1}, 'senderId': {$ne: ?1}}", sort = "{createAt: -1}", count = true)
    Long countNewMessage(String roomId, String userId);

}
