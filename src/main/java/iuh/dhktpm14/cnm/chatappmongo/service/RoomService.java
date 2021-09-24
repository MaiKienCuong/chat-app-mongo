package iuh.dhktpm14.cnm.chatappmongo.service;

import iuh.dhktpm14.cnm.chatappmongo.entity.Member;
import iuh.dhktpm14.cnm.chatappmongo.entity.Room;
import iuh.dhktpm14.cnm.chatappmongo.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class RoomService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RoomRepository roomRepository;

    /**
     * thêm một list member vào room
     */
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
        if (optionalMember.isPresent() && optionalMember.get().getIsAdmin() != null)
            return optionalMember.get().getIsAdmin();
        return false;
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

    /**
     * xóa trong database
     */
    private void delete(String deleteId, String roomId) {
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
        if (roomRepository.isCreatorRoom(currentUserId, roomId) || isAdminOfRoom(currentUserId, roomId)) {
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
        var criteria = Criteria.where("_id").is(roomId);
        var update = new Update();
        update.set("name", newName);
        mongoTemplate.updateFirst(Query.query(criteria), update, Room.class);
    }
}
