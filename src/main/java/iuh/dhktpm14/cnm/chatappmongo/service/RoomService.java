package iuh.dhktpm14.cnm.chatappmongo.service;

import iuh.dhktpm14.cnm.chatappmongo.entity.Member;
import iuh.dhktpm14.cnm.chatappmongo.entity.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public void addMembersToRoom(List<Member> members, String toRoomId) {
        BulkOperations ops = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Room.class);
        var i = 0;
        for (Member member : members) {
            var criteria = Criteria.where("_id").is(toRoomId);
            var update = new Update();
            update.push("members", member);
            ops.updateOne(Query.query(criteria), update);
            i++;
            if (i % 20 == 0)
                ops.execute();
        }
        if (i != 0)
            ops.execute();
    }

}
