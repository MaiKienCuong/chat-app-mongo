package iuh.dhktpm14.cnm.chatappmongo.service;

import com.mongodb.client.result.DeleteResult;
import iuh.dhktpm14.cnm.chatappmongo.entity.Inbox;
import iuh.dhktpm14.cnm.chatappmongo.entity.Room;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.RoomType;
import iuh.dhktpm14.cnm.chatappmongo.projection.Count;
import iuh.dhktpm14.cnm.chatappmongo.projection.CustomAggregationOperation;
import iuh.dhktpm14.cnm.chatappmongo.repository.InboxRepository;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
public class InboxService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private InboxRepository inboxRepository;

    private static final Logger logger = Logger.getLogger(InboxService.class.getName());

    public Page<Inbox> searchAllInboxByName(String currentUserId, String query, String type, Pageable pageable) {
        log.info("query = {}", query);
        log.info("type = '{}'", type);
        int count = countSearchInboxByName(currentUserId, query, type);
        log.info("count = '{}'", count);
        if (count == 0)
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        var aggregation = Aggregation.newAggregation(
                new CustomAggregationOperation("{$match: {ofUserId: '" + currentUserId + "', empty: false}}"),
                new CustomAggregationOperation("{$lookup: {from: 'room', let: {rId: '$roomId'}, pipeline:[{$match: {type: {$regex: /.*" + type + ".*/}}},{$match: {$expr: {$eq: [{$toString: '$_id'}, '$$rId']}}}],  as: 'room'}}"),
                Aggregation.unwind("room"),
                Aggregation.unwind("room.members"),
                new CustomAggregationOperation("{$match: {'room.members.userId': {$ne: '" + currentUserId + "'}}}"),
                new CustomAggregationOperation("{$lookup: {from: 'user',let: {uId: '$room.members.userId'},  pipeline: [{$match: {$expr: {$eq: [{$toString: '$_id'}, '$$uId']}}}], as: 'user'}}"),
                Aggregation.unwind("user"),
                new CustomAggregationOperation("{$match: {$or: [{$and: [{'room.type': 'GROUP'}, {'room.name': {$regex: /.*" + query + ".*/}}]}, {$and: [{'room.type': 'ONE'}, {'user.displayName': {$regex: /.*" + query + ".*/i}}]}]}}"),
                new CustomAggregationOperation("{$project: {room:0, user:0}}"),
                new CustomAggregationOperation("{$group: {_id: '$_id', doc: {$addToSet: '$$ROOT'}}}"),
                new CustomAggregationOperation("{$unwind: '$doc'}, {$project: {_id:0}}"),
                new CustomAggregationOperation("{$replaceRoot: {newRoot: '$doc'}}"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "lastTime")),
                Aggregation.skip(((long) pageable.getPageNumber() * pageable.getPageSize())),
                Aggregation.limit(pageable.getPageSize())
        );

        AggregationResults<Inbox> results = mongoTemplate.aggregate(aggregation, "inbox", Inbox.class);
        return new PageImpl<>(results.getMappedResults(), pageable, count);
    }

    private int countSearchInboxByName(String currentUserId, String query, String type) {
        var aggregation = Aggregation.newAggregation(
                new CustomAggregationOperation("{$match: {ofUserId: '" + currentUserId + "', empty: false}}"),
                new CustomAggregationOperation("{$lookup: {from: 'room', let: {rId: '$roomId'}, pipeline:[{$match: {type: {$regex: /.*" + type + ".*/}}},{$match: {$expr: {$eq: [{$toString: '$_id'}, '$$rId']}}}],  as: 'room'}}"),
                Aggregation.unwind("room"),
                Aggregation.unwind("room.members"),
                new CustomAggregationOperation("{$match: {'room.members.userId': {$ne: '" + currentUserId + "'}}}"),
                new CustomAggregationOperation("{$lookup: {from: 'user',let: {uId: '$room.members.userId'},  pipeline: [{$match: {$expr: {$eq: [{$toString: '$_id'}, '$$uId']}}}], as: 'user'}}"),
                Aggregation.unwind("user"),
                new CustomAggregationOperation("{$match: {$or: [{$and: [{'room.type': 'GROUP'}, {'room.name': {$regex: /.*" + query + ".*/}}]}, {$and: [{'room.type': 'ONE'}, {'user.displayName': {$regex: /.*" + query + ".*/i}}]}]}}"),
                new CustomAggregationOperation("{$project: {room:0, user:0}}"),
                new CustomAggregationOperation("{$group: {_id: '$_id', doc: {$addToSet: '$$ROOT'}}}"),
                Aggregation.group().count().as("count")
        );

        AggregationResults<Count> results = mongoTemplate.aggregate(aggregation, "inbox", Count.class);
        List<Count> countList = results.getMappedResults();
        if (countList.isEmpty())
            return 0;
        if (countList.get(0) == null)
            return 0;
        return countList.get(0).getCount();
    }

    public Page<Inbox> searchOnlyInboxGroupByName(String currentUserId, String query, Pageable pageable) {
        int count = countSearchOnlyInboxGroup(currentUserId, query);
        if (count == 0)
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        var aggregation = Aggregation.newAggregation(
                new CustomAggregationOperation("{$match: {ofUserId: '" + currentUserId + "', empty: false}}"),
                new CustomAggregationOperation("{$lookup: {from: 'room', let: {roomId: '$roomId'}, pipeline: [{$match: {$expr: {$eq: [{$toString: '$_id'}, '$$roomId']}}}], as: 'room'}}"),
                Aggregation.unwind("room"),
                new CustomAggregationOperation("{$match: {'room.type': '" + RoomType.GROUP.toString() + "'}}"),
                new CustomAggregationOperation("{$match: {'room.name': {$regex: /.*" + query + ".*/}}}"),
                new CustomAggregationOperation("{$project: {room: 0}}"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "lastTime")),
                Aggregation.skip(((long) pageable.getPageNumber() * pageable.getPageSize())),
                Aggregation.limit(pageable.getPageSize())
        );

        AggregationResults<Inbox> results = mongoTemplate.aggregate(aggregation, "inbox", Inbox.class);
        return new PageImpl<>(results.getMappedResults(), pageable, count);
    }

    private int countSearchOnlyInboxGroup(String currentUserId, String query) {
        var aggregation = Aggregation.newAggregation(
                new CustomAggregationOperation("{$match: {ofUserId: '" + currentUserId + "', empty: false}}"),
                new CustomAggregationOperation("{$lookup: {from: 'room', let: {roomId: '$roomId'}, pipeline: [{$match: {$expr: {$eq: [{$toString: '$_id'}, '$$roomId']}}}], as: 'room'}}"),
                Aggregation.unwind("room"),
                new CustomAggregationOperation("{$match: {'room.type': '" + RoomType.GROUP.toString() + "'}}"),
                new CustomAggregationOperation("{$match: {'room.name': {$regex: /.*" + query + ".*/}}}"),
                new CustomAggregationOperation("{$project: {room: 0}}"),
                Aggregation.group().count().as("count")
        );

        AggregationResults<Count> results = mongoTemplate.aggregate(aggregation, "inbox", Count.class);
        List<Count> countList = results.getMappedResults();
        if (countList.isEmpty())
            return 0;
        if (countList.get(0) == null)
            return 0;
        return countList.get(0).getCount();
    }

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

    public List<Inbox> findAllByRoomId(String roomId) {
        return inboxRepository.findAllByRoomId(roomId);
    }

    public long deleteAllByRoomId(String roomId) {
        return inboxRepository.deleteAllByRoomId(roomId);
    }

}
