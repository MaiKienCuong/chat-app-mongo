package iuh.dhktpm14.cnm.chatappmongo.service;

import iuh.dhktpm14.cnm.chatappmongo.entity.Member;
import iuh.dhktpm14.cnm.chatappmongo.entity.ReadTracking;
import iuh.dhktpm14.cnm.chatappmongo.entity.Room;
import iuh.dhktpm14.cnm.chatappmongo.repository.MessageRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.ReadTrackingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ReadTrackingService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ReadTrackingRepository readTrackingRepository;

    @Autowired
    private MessageRepository messageRepository;

    public void resetUnreadMessageToZero(String roomId, String userId) {
        var criteria = Criteria.where("roomId").is(roomId).and("userId").is(userId);
        var update = new Update();
        update.set("unReadMessage", 0);
        mongoTemplate.updateFirst(Query.query(criteria), update, ReadTracking.class);
    }

    public void updateReadTracking(String userId, String roomId) {
        var readTracking = readTrackingRepository.findByRoomIdAndUserId(roomId, userId);
        if (readTracking != null && readTracking.getUnReadMessage() != 0) {
            var lastMessage = messageRepository.getLastMessageOfRoom(roomId);
            var criteria = Criteria.where("roomId").is(roomId).and("userId").is(userId);
            var update = new Update();
            update.set("messageId", lastMessage.getId());
            update.set("readAt", new Date());
            update.set("unReadMessage", 0);
            mongoTemplate.updateFirst(Query.query(criteria), update, ReadTracking.class);
        }
    }

    public void incrementUnReadMessage(Room room, String currentUserId) {
        BulkOperations ops = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, ReadTracking.class);
        var i = 0;
        for (Member member : room.getMembers()) {
            if (! currentUserId.equals(member.getUserId())) {
                var criteria = Criteria.where("roomId").is(room.getId())
                        .and("userId").is(member.getUserId());
                var update = new Update();
                update.inc("unReadMessage", 1);
                ops.updateOne(Query.query(criteria), update);
                i++;
                if (i % 20 == 0)
                    ops.execute();
            }
        }
        if (i != 0)
            ops.execute();
    }

}
