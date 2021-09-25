package iuh.dhktpm14.cnm.chatappmongo.service;

import iuh.dhktpm14.cnm.chatappmongo.entity.Inbox;
import iuh.dhktpm14.cnm.chatappmongo.entity.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class InboxService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public void updateEmptyStatusInbox(String inboxId, boolean emptyStatus) {
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
        var criteria = Criteria.where("roomId").is(room.getId());
        var update = new Update();
        update.set("lastTime", new Date());
        mongoTemplate.updateMulti(Query.query(criteria), update, Inbox.class);
    }
}
