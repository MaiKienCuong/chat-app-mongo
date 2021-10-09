package iuh.dhktpm14.cnm.chatappmongo.service;

import com.mongodb.client.result.DeleteResult;
import iuh.dhktpm14.cnm.chatappmongo.entity.Inbox;
import iuh.dhktpm14.cnm.chatappmongo.entity.Room;
import iuh.dhktpm14.cnm.chatappmongo.repository.InboxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class InboxService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private InboxRepository inboxRepository;

    private static final Logger logger = Logger.getLogger(InboxService.class.getName());

    /*
    cập nhật xem inbox này có rỗng hay là không, khi người dùng xóa inbox thì cập nhật thành true
     */
    public void updateEmptyStatusInbox(String inboxId, boolean emptyStatus) {
        logger.log(Level.INFO, "set empty status = {0} for inboxId = {1}",
                new Object[]{ emptyStatus, inboxId });
        var criteria = Criteria.where("_id").is(inboxId);
        var update = new Update();
        update.set("empty", emptyStatus);
        mongoTemplate.updateFirst(Query.query(criteria), update, Inbox.class);
    }

    /**
     * cập nhật thời gian hoạt động lần cuối cho tất cả inbox của room
     * dùng field này để sắp xếp inbox khi có nhiều trang
     */
    public void updateLastTimeForAllInboxOfRoom(Room room) {
        logger.log(Level.INFO, "update last time for all inbox of roomId = {0}", room.getId());
        var criteria = Criteria.where("roomId").is(room.getId());
        var update = new Update();
        update.set("lastTime", new Date());
        mongoTemplate.updateMulti(Query.query(criteria), update, Inbox.class);
    }

    /**
     * lấy tất cả inbox của userId mà inbox đó không trống
     */
    public Page<Inbox> getAllInboxOfUser(String ofUserId, Pageable pageable) {
        return inboxRepository.getAllInboxOfUser(ofUserId, pageable);
    }

    /**
     *
     */
    public boolean existsByOfUserIdAndRoomId(String ofUserId, String roomId) {
        return inboxRepository.existsByOfUserIdAndRoomId(ofUserId, roomId);
    }

    /**
     * tìm inbox theo userId và roomId
     */
    public Optional<Inbox> findByOfUserIdAndRoomId(String ofUserId, String roomId) {
        return inboxRepository.findByOfUserIdAndRoomId(ofUserId, roomId);
    }

    /**
     * kiểm tra xem inboxId có phải thuộc về người này hay không
     */
    public boolean existsByIdAndOfUserId(String id, String ofUserId) {
        return inboxRepository.existsByIdAndOfUserId(id, ofUserId);
    }

    /**
     *
     */
    public Optional<Inbox> findByIdAndOfUserId(String id, String ofUserId) {
        return inboxRepository.findByIdAndOfUserId(id, ofUserId);
    }

    public Inbox save(Inbox inbox) {
        return inboxRepository.save(inbox);
    }

    public Optional<Inbox> findById(String inboxId) {
        return inboxRepository.findById(inboxId);
    }

    public void deleteInbox(String ofUserId, String roomId) {
        logger.log(Level.INFO, "delete inbox of userId = {0} in roomId = {1}", new Object[]{ ofUserId, roomId });
        var criteria = Criteria.where("ofUserId").is(ofUserId)
                .and("roomId").is(roomId);
        DeleteResult remove = mongoTemplate.remove(Query.query(criteria), Inbox.class);
        logger.log(Level.INFO, "number of row deleted = {0}", remove.getDeletedCount());
    }

}
