package iuh.dhktpm14.cnm.chatappmongo.service;

import iuh.dhktpm14.cnm.chatappmongo.dto.StatisticsByMonth;
import iuh.dhktpm14.cnm.chatappmongo.entity.Inbox;
import iuh.dhktpm14.cnm.chatappmongo.entity.InboxMessage;
import iuh.dhktpm14.cnm.chatappmongo.entity.Message;
import iuh.dhktpm14.cnm.chatappmongo.entity.Reaction;
import iuh.dhktpm14.cnm.chatappmongo.entity.Room;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.MessageType;
import iuh.dhktpm14.cnm.chatappmongo.projection.Count;
import iuh.dhktpm14.cnm.chatappmongo.projection.CustomAggregationOperation;
import iuh.dhktpm14.cnm.chatappmongo.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class MessageService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private RoomService roomService;

    @Autowired
    private InboxService inboxService;

    @Autowired
    private InboxMessageService inboxMessageService;

    private static final Logger logger = Logger.getLogger(MessageService.class.getName());

    /*
    gỡ một tin nhắn
     */
    public void deleteMessage(String messageId, String newContent) {
        logger.log(Level.INFO, "deleting messageId = {0}, new content = {1}",
                new Object[]{ messageId, newContent });
        var criteria = Criteria.where("_id").is(messageId);
        var update = new Update();
        update.set("content", newContent);
        update.set("deleted", true);
        update.unset("media");
        update.set("type", MessageType.TEXT);
        mongoTemplate.updateFirst(Query.query(criteria), update, Message.class);
    }

    /*
    đánh dấu tất cả tin nhắn trong room đối với người này là đã xóa
     */
    public void deleteInbox(String roomId, String userId) {
        var criteria = Criteria.where("roomId").is(roomId);
        var update = new Update();
        update.addToSet("userDelete", userId);
        mongoTemplate.updateMulti(Query.query(criteria), update, Message.class);
    }

    /*
    thêm cảm xúc vào tin nhắn
     */
    public void addReactToMessage(String messageId, Reaction reaction) {
        logger.log(Level.INFO, "adding reaction = {0}, to messageId = {1}",
                new Object[]{ reaction, messageId });
        var criteria = Criteria.where("_id").is(messageId);
        var update = new Update();
        update.push("reactions", reaction);
        mongoTemplate.updateFirst(Query.query(criteria), update, Message.class);
    }

    /*
    kiểm tra tin nhắn có thuộc quyền của user hay không, user có quyền xem tin nhắn hay không
     */
    public boolean checkPermissionToSeeMessage(String messageId, String userId) {
        Optional<Message> messageOptional = messageRepository.findById(messageId);
        if (messageOptional.isEmpty())
            return false;

        var message = messageOptional.get();
        Optional<Room> roomOptional = roomService.findById(message.getRoomId());
        if (roomOptional.isEmpty())
            return false;

        var room = roomOptional.get();
        if (! roomService.isMemberOfRoom(userId, room.getId()))
            return false;

        Optional<Inbox> inboxOptional = inboxService.findByOfUserIdAndRoomId(userId, room.getId());
        if (inboxOptional.isEmpty())
            return false;

        var inbox = inboxOptional.get();
        if (inbox.isEmpty())
            return false;

        return inboxMessageService.existsByInboxIdAndMessageId(inbox.getId(), messageId);
    }

    public Optional<Message> getLastMessageOfRoom(String userId, String roomId) {
        Optional<Inbox> inboxOptional = inboxService.findByOfUserIdAndRoomId(userId, roomId);
        if (inboxOptional.isPresent()) {
            var inbox = inboxOptional.get();
            Optional<InboxMessage> inboxMessageOptional = inboxMessageService.getLastMessageByInbox(inbox.getId());
            if (inboxMessageOptional.isPresent()) {
                var inboxMessage = inboxMessageOptional.get();
                if (inboxMessage.getMessageId() != null)
                    return messageRepository.findById(inboxMessage.getMessageId());
            }
        }
        return Optional.empty();
    }

    /**
     * lấy tất cả tin nhắn của inbox, khi gọi sẽ truyền messageIds=inbox.getMessageIds()
     */
    public Page<Message> findAllByIdInMessageIdsPaged(List<String> messageIds, Pageable pageable) {
        return messageRepository.findAllByIdInMessageIdsPaged(messageIds, pageable);
    }

    public List<Message> findAllByIdInMessageIds(List<String> messageIds, Pageable pageable) {
        return messageRepository.findAllByIdInMessageIds(messageIds, pageable);
    }

    public Message save(Message message) {
        return messageRepository.save(message);
    }

    public Optional<Message> findById(String messageId) {
        return messageRepository.findById(messageId);
    }

    public List<Message> findByCreateAtBetween(Date from, Date to) {
        return messageRepository.findByCreateAtBetween(from, to);
    }

    public List<StatisticsByMonth> statisticsByMonths(int year) {
        var aggregation = Aggregation.newAggregation(
                new CustomAggregationOperation("{$project:{_id:0,year:{$year:'$createAt'},month:{$month:'$createAt'}}}"),
                new CustomAggregationOperation("{$match:{year:{$eq:" + year + "}}}"),
                new CustomAggregationOperation("{$group:{_id:'$month',sum:{$sum:1}}}"),
                new CustomAggregationOperation("{$project:{_id:0,month:'$_id',sum:1}}"),
                Aggregation.sort(Sort.by(Sort.Direction.ASC, "month"))
        );

        AggregationResults<StatisticsByMonth> statistics = mongoTemplate.aggregate(aggregation, "message", StatisticsByMonth.class);
        return statistics.getMappedResults();
    }

    public long deleteAllByRoomId(String roomId) {
        return messageRepository.deleteAllByRoomId(roomId);
    }

    public Page<Message> getListMessageByType(String roomId, String userId, List<String> typeOfMedia, Pageable pageable) {
        long count = countByType(roomId, userId, typeOfMedia);
        if (count == 0)
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        var condition = getCriteriaUserDeleteNotInUserId(userId);

        var aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("roomId").is(roomId)),
                new CustomAggregationOperation("{$match: {type: 'MEDIA', media: {$exists: true}}}"),
                Aggregation.match(condition),
                new CustomAggregationOperation("{$unwind: '$media'}"),
                Aggregation.match(Criteria.where("media.type").in(typeOfMedia)),
                new CustomAggregationOperation("{$project: {'readbyes': 0, 'reactions': 0, 'reply': 0}}"),
                new CustomAggregationOperation("{$group: {_id: '$$ROOT', newMedia: {$addToSet: '$media'}}}"),
                new CustomAggregationOperation("{$set: {'_id.media': '$newMedia'}}"),
                new CustomAggregationOperation("{$replaceRoot: {newRoot: '$_id'}}"),

                new CustomAggregationOperation("{$sort: {createAt: -1, 'media.name':-1}}"),
                Aggregation.skip(((long) pageable.getPageNumber() * pageable.getPageSize())),
                Aggregation.limit(pageable.getPageSize())

        );

        AggregationResults<Message> messages = mongoTemplate.aggregate(aggregation, "message", Message.class);
        return new PageImpl<>(messages.getMappedResults(), pageable, count);
    }

    private long countByType(String roomId, String userId, List<String> typeOfMedia) {
        var condition = getCriteriaUserDeleteNotInUserId(userId);

        var aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("roomId").is(roomId)),
                new CustomAggregationOperation("{$match: {type: 'MEDIA', media: {$exists: true}}}"),
                Aggregation.match(condition),
                new CustomAggregationOperation("{$unwind: '$media'}"),
                Aggregation.match(Criteria.where("media.type").in(typeOfMedia)),
                Aggregation.group().count().as("count")
        );

        AggregationResults<Count> messages = mongoTemplate.aggregate(aggregation, "message", Count.class);
        return getCountFromAggregationResultsCount(messages);
    }

    private Criteria getCriteriaUserDeleteNotInUserId(String userId) {
        final var columnUserDelete = "userDelete";
        Criteria notIn = Criteria.where(columnUserDelete).nin(Collections.singletonList(userId));
        Criteria empty = Criteria.where(columnUserDelete).size(0);
        Criteria notInOrEmpty = new Criteria().orOperator(notIn, empty);

        var exists = new Criteria();
        exists.andOperator(Criteria.where(columnUserDelete).exists(true), notInOrEmpty);

        var condition = new Criteria();
        condition.orOperator(exists, Criteria.where(columnUserDelete).is(null));
        return condition;
    }

    private int getCountFromAggregationResultsCount(AggregationResults<Count> results) {
        List<Count> mappedResults = results.getMappedResults();
        if (mappedResults.isEmpty())
            return 0;
        if (mappedResults.get(0) == null)
            return 0;
        return mappedResults.get(0).getCount();
    }

    public Page<Message> findAllByTypeLinkOrText(String roomId, List<String> userDelete, List<String> type, Pageable pageable) {
        return messageRepository.findAllByTypeLinkOrText(roomId, userDelete, type, pageable);
    }

}
