package iuh.dhktpm14.cnm.chatappmongo.service;

import iuh.dhktpm14.cnm.chatappmongo.entity.ReadTracking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class ReadTrackingService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public void resetUnreadMessageToZero(String roomId, String userId) {
        var criteria = Criteria.where("roomId").is(roomId).and("userId").is(userId);
        var update = new Update();
        update.set("unReadMessage", 0);
        mongoTemplate.updateFirst(Query.query(criteria), update, ReadTracking.class);
    }

}
