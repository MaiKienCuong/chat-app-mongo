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

    public void sendAfterAddMember(Room room) {
        if (valid(room)) {
            for (Member member : room.getMembers()) {
                messagingTemplate.convertAndSendToUser(member.getUserId(), "/queue/room/members/add",
                        roomMapper.toRoomSummaryDto(room));
            }
        }
    }

    public void sendAfterDeleteMember(Room room) {
        if (valid(room)) {
            for (Member member : room.getMembers()) {
                messagingTemplate.convertAndSendToUser(member.getUserId(), "/queue/room/members/delete",
                        roomMapper.toRoomSummaryDto(room));
            }
        }
    }

    private boolean valid(Room room) {
        if (room == null)
            return false;
        return room.getId() != null;
    }

}
