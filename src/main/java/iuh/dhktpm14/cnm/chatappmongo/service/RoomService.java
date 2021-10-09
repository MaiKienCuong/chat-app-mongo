package iuh.dhktpm14.cnm.chatappmongo.service;

import iuh.dhktpm14.cnm.chatappmongo.entity.Member;
import iuh.dhktpm14.cnm.chatappmongo.entity.Room;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class RoomService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RoomRepository roomRepository;

    private static final Logger logger = Logger.getLogger(RoomService.class.getName());

    /**
     * thêm một list member vào room
     */
    public void addMembersToRoom(List<Member> members, String toRoomId) {
        logger.log(Level.INFO, "adding list members = {0}, to roomId = {1}",
                new Object[]{ members, toRoomId });

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

    /**
     * lấy thành viên trong room theo id của member
     */
    public Optional<Member> getMemberFromRoomById(String userId, String roomId) {
        Optional<Room> roomOptional = roomRepository.findById(roomId);
        if (roomOptional.isPresent()) {
            var room = roomOptional.get();
            Set<Member> members = room.getMembers();
            if (members != null && ! members.isEmpty()) {
                return members.stream().filter(x -> x.getUserId().equals(userId)).findFirst();
            }
        }
        return Optional.empty();
    }

    /**
     * kiểm tra xem userId này có phải admin của room hay không
     */
    public boolean isAdminOfRoom(String userId, String roomId) {
        Optional<Member> optionalMember = getMemberFromRoomById(userId, roomId);
        return optionalMember.map(Member::isAdmin).orElse(false);
    }

    public boolean deleteMember(String deleteId, String roomId, String currentUserId) {
        if (deleteId.equals(currentUserId))
            return false;
        if (canDelete(deleteId, roomId, currentUserId)) {
            delete(deleteId, roomId);
            return true;
        }
        return false;
    }

    public void leaveGroup(String userId, String roomId) {
        logger.log(Level.INFO, "[leave group] userId = {0} will leave roomId = {1}", new Object[]{ userId, roomId });
        var currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        logger.log(Level.INFO, "[leave group] currentUser = {0}", currentUser);
        if (currentUser != null && currentUser.getId() != null && userId.equals(currentUser.getId())) {
            delete(userId, roomId);
        }
    }

    /**
     * xóa trong database
     */
    private void delete(String deleteId, String roomId) {
        logger.log(Level.INFO, "deleting member useId = {0}, in roomId = {1}",
                new Object[]{ deleteId, roomId });

        var criteria = Criteria.where("_id").is(roomId);
        var update = new Update();
        update.pull("members", Query.query(Criteria.where("userId").is(deleteId)));
        mongoTemplate.updateFirst(Query.query(criteria), update, Room.class);
    }

    private boolean canDelete(String deleteId, String roomId, String currentUserId) {
        Optional<Member> optionalMember = getMemberFromRoomById(deleteId, roomId);
        if (optionalMember.isPresent()) {
            if (roomRepository.isCreatorRoom(currentUserId, roomId))
                return true;
            if (isAdminOfRoom(currentUserId, roomId)) {
                return ! isAdminOfRoom(deleteId, roomId);
            }
            return currentUserId.equals(optionalMember.get().getAddByUserId());
        }
        return false;
    }

    /**
     * thêm thành viên làm admin room
     */
    public boolean setAdmin(String userId, String roomId, String currentUserId) {
        logger.log(Level.INFO, "in function set admin for userId = {0}, in roomId = {1}",
                new Object[]{ userId, roomId });

        if (roomRepository.isCreatorRoom(currentUserId, roomId) || isAdminOfRoom(currentUserId, roomId)) {
            logger.log(Level.INFO, "setting admin ...");

            var criteria = Criteria.where("_id").is(roomId).and("members.userId").is(userId);
            var update = new Update();
            update.set("members.$.isAdmin", true);
            mongoTemplate.updateFirst(Query.query(criteria), update, Room.class);
            return true;
        }
        return false;
    }

    /**
     * đổi tên cuộc trò chuyện nhóm
     */
    public void renameRoom(String roomId, String newName) {
        logger.log(Level.INFO, "rename roomId = {0}, new name = {1}",
                new Object[]{ roomId, newName });

        var criteria = Criteria.where("_id").is(roomId);
        var update = new Update();
        update.set("name", newName);
        mongoTemplate.updateFirst(Query.query(criteria), update, Room.class);
    }

    public Optional<Room> findById(String roomId) {
        return roomRepository.findById(roomId);
    }

    public Room save(Room room) {
        return roomRepository.save(room);
    }

    public Room findCommonRoomBetween(String id, String anotherUserId) {
        return roomRepository.findCommonRoomBetween(id, anotherUserId);
    }

    public boolean isMemberOfRoom(String userId, String roomId) {
        return roomRepository.isMemberOfRoom(userId, roomId);
    }

    public long countCommonGroupBetween(String userId, String anotherUserId) {
        return roomRepository.countCommonGroupBetween(userId, anotherUserId);
    }

    public List<Room> findCommonGroupBetween(String userId, String anotherUserId) {
        return roomRepository.findCommonGroupBetween(userId, anotherUserId);
    }
}
