package iuh.dhktpm14.cnm.chatappmongo.service;

import iuh.dhktpm14.cnm.chatappmongo.entity.Member;
import iuh.dhktpm14.cnm.chatappmongo.entity.ReadTracking;
import iuh.dhktpm14.cnm.chatappmongo.entity.Room;
import iuh.dhktpm14.cnm.chatappmongo.repository.ReadTrackingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class ReadTrackingService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ReadTrackingRepository readTrackingRepository;

    private static final Logger logger = Logger.getLogger(ReadTrackingService.class.getName());

    /**
     * cập nhật số tin nhắn chưa đọc thành 0
     */
    public void resetUnreadMessageToZero(String roomId, String userId) {
        logger.log(Level.INFO, "reset unread message to 0 for userId = {0}, in roomId = {1}",
                new Object[]{ userId, roomId });

        var criteria = Criteria.where("roomId").is(roomId).and("userId").is(userId);
        var update = new Update();
        update.set("unReadMessage", 0);
        mongoTemplate.updateFirst(Query.query(criteria), update, ReadTracking.class);
    }

    /**
     * cập nhật tin nhắn đã đọc
     */
    public void updateReadTracking(String userId, String roomId, String messageId) {
        var readTracking = readTrackingRepository.findByRoomIdAndUserId(roomId, userId);
        if (readTracking != null) {
            logger.log(Level.INFO, "read tracking of userId = {0}, in roomId = {1} is not null",
                    new Object[]{ userId, roomId });

            logger.log(Level.INFO, "old message id of read tracking = {0}, unread message = {1}",
                    new Object[]{ readTracking.getMessageId(), readTracking.getUnReadMessage() });

            if (messageId != null && (! messageId.equals(readTracking.getMessageId()))) {

                logger.log(Level.INFO, "updating read tracking of userId = {0}, new messageId = {1}, in roomId = {2}",
                        new Object[]{ userId, messageId, roomId });

                var criteria = Criteria.where("roomId").is(roomId).and("userId").is(userId);
                var update = new Update();
                update.set("messageId", messageId);
                update.set("readAt", new Date());
                update.set("unReadMessage", 0);
                mongoTemplate.updateFirst(Query.query(criteria), update, ReadTracking.class);
            }
        } else {
            logger.log(Level.INFO, "read tracking of userId = {0}, new messageId = {1}, in roomId = {2} is null, creating new...",
                    new Object[]{ userId, messageId, roomId });

            ReadTracking tracking = ReadTracking.builder()
                    .roomId(roomId)
                    .userId(userId)
                    .messageId(messageId)
                    .build();
            readTrackingRepository.save(tracking);
        }
    }

    /**
     * set số tin nhắn mới chưa đọc tăng lên 1 khi có tin nhắn mới
     */
    public void incrementUnReadMessageForMembersOfRoomExcludeUserId(Room room, String currentUserId) {
        logger.log(Level.INFO, "increment unread message of roomId = {0}, exclude userId = {1}",
                new Object[]{ room.getId(), currentUserId });

        BulkOperations ops = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, ReadTracking.class);
        var i = 0;
        for (Member member : room.getMembers()) {
            if (! currentUserId.equals(member.getUserId())) {
                if (readTrackingRepository.findByRoomIdAndUserId(room.getId(), member.getUserId()) != null) {
                    logger.log(Level.INFO, "increment unread message number for userId = {0}, in roomId = {1}",
                            new Object[]{ member.getUserId(), room.getId() });

                    i = incrementUnReadMessage(room, ops, i, member);
                } else {
                    logger.log(Level.INFO, "create new unread message number for userId = {0}, in roomId = {1}",
                            new Object[]{ member.getUserId(), room.getId() });

                    createNewReadTracking(room, member);
                }
            }
        }
        if (i != 0)
            ops.execute();
    }

    public void incrementUnReadMessageForAllMember(Room room) {
        logger.log(Level.INFO, "increment unread message to all member of roomId = {0}",
                new Object[]{ room.getId() });
        BulkOperations ops = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, ReadTracking.class);
        var i = 0;
        for (Member member : room.getMembers()) {
            if (readTrackingRepository.findByRoomIdAndUserId(room.getId(), member.getUserId()) != null) {
                logger.log(Level.INFO, "increment unread message number for userId = {0}, in roomId = {1}",
                        new Object[]{ member.getUserId(), room.getId() });
                i = incrementUnReadMessage(room, ops, i, member);
            } else {
                logger.log(Level.INFO, "create new unread message number for userId = {0}, in roomId = {1}",
                        new Object[]{ member.getUserId(), room.getId() });
                createNewReadTracking(room, member);
            }
        }
        if (i != 0)
            ops.execute();
    }

    private void createNewReadTracking(Room room, Member member) {
        logger.log(Level.INFO, "creating new read tracking for memberId = {0}, in roomId = {1}",
                new Object[]{ member.getUserId(), room.getId() });
        var readTracking = ReadTracking.builder()
                .unReadMessage(1)
                .roomId(room.getId())
                .userId(member.getUserId())
                .build();
        readTrackingRepository.save(readTracking);
    }

    private int incrementUnReadMessage(Room room, BulkOperations ops, int i, Member member) {
        var criteria = Criteria.where("roomId").is(room.getId())
                .and("userId").is(member.getUserId());
        var update = new Update();
        update.inc("unReadMessage", 1);
        ops.updateOne(Query.query(criteria), update);
        i++;
        if (i % 20 == 0)
            ops.execute();
        return i;
    }


    public ReadTracking findByRoomIdAndUserId(String roomId, String id) {
        return readTrackingRepository.findByRoomIdAndUserId(roomId, id);
    }

    public List<ReadTracking> findAllByMessageId(String id) {
        return readTrackingRepository.findAllByMessageId(id);
    }

    public long deleteAllByRoomId(String roomId) {
        return readTrackingRepository.deleteAllByRoomId(roomId);
    }

}
