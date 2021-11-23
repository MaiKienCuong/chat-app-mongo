package iuh.dhktpm14.cnm.chatappmongo.service;

import iuh.dhktpm14.cnm.chatappmongo.entity.Member;
import iuh.dhktpm14.cnm.chatappmongo.entity.Room;
import iuh.dhktpm14.cnm.chatappmongo.mapper.RoomMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RoomRestSocketService {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private RoomMapper roomMapper;

    public void sendAfterSetAdmin(Room room) {
        if (valid(room)) {
            var roomSummaryDto = roomMapper.toRoomSummaryDto(room);
            for (Member member : room.getMembers()) {
                messagingTemplate.convertAndSendToUser(member.getUserId(), "/queue/room/members/admin/setNew",
                        roomSummaryDto);
            }
        }
    }

    public void sendAfterRecallAdmin(Room room) {
        if (valid(room)) {
            var roomSummaryDto = roomMapper.toRoomSummaryDto(room);
            for (Member member : room.getMembers()) {
                messagingTemplate.convertAndSendToUser(member.getUserId(), "/queue/room/members/admin/recall",
                        roomSummaryDto);
            }
        }
    }

    public void sendAfterAddMember(Room room) {
        if (valid(room)) {
            var roomSummaryDto = roomMapper.toRoomSummaryDto(room);
            for (Member member : room.getMembers()) {
                messagingTemplate.convertAndSendToUser(member.getUserId(), "/queue/room/members/add",
                        roomSummaryDto);
            }
        }
    }

    public void sendAfterDeleteMember(Room room) {
        if (valid(room)) {
            var roomSummaryDto = roomMapper.toRoomSummaryDto(room);
            for (Member member : room.getMembers()) {
                messagingTemplate.convertAndSendToUser(member.getUserId(), "/queue/room/members/delete",
                        roomSummaryDto);
            }
        }
    }

    public void sendAfterRename(Room room) {
        if (valid(room)) {
            var roomSummaryDto = roomMapper.toRoomSummaryDto(room);
            for (Member member : room.getMembers()) {
                messagingTemplate.convertAndSendToUser(member.getUserId(), "/queue/room/rename",
                        roomSummaryDto);
            }
        }
    }

    public void sendAfterChangeImage(Room room) {
        if (valid(room)) {
            var roomSummaryDto = roomMapper.toRoomSummaryDto(room);
            for (Member member : room.getMembers()) {
                messagingTemplate.convertAndSendToUser(member.getUserId(), "/queue/room/changeImage",
                        roomSummaryDto);
            }
        }
    }

    private boolean valid(Room room) {
        if (room == null)
            return false;
        return room.getId() != null;
    }

}
