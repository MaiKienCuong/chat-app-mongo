package iuh.dhktpm14.cnm.chatappmongo.service;

import iuh.dhktpm14.cnm.chatappmongo.entity.Inbox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

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

}
