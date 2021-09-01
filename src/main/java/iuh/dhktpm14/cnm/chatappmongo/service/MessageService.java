package iuh.dhktpm14.cnm.chatappmongo.service;

import iuh.dhktpm14.cnm.chatappmongo.entity.Message;
import iuh.dhktpm14.cnm.chatappmongo.entity.Reaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public void deleteMessage(String messageId, String newContent) {
        var criteria = Criteria.where("_id").is(messageId);
        var update = new Update();
        update.set("content", newContent);
        update.set("deleted", true);
        mongoTemplate.updateFirst(Query.query(criteria), update, Message.class);
    }

    public void addReactToMessage(String messageId, Reaction reaction) {
        var criteria = Criteria.where("_id").is(messageId);
        var update = new Update();
        update.push("reactions", reaction);
        mongoTemplate.updateFirst(Query.query(criteria), update, Message.class);
    }

}
