package iuh.dhktpm14.cnm.chatappmongo.mapper;

import iuh.dhktpm14.cnm.chatappmongo.dto.RoomGroupSummaryDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.RoomOneSummaryDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.Room;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.RoomType;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.MyException;
import iuh.dhktpm14.cnm.chatappmongo.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RoomMapper {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserMapper userMapper;

    public Object toRoomSummaryDto(String roomId) {
        var currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (currentUser == null)
            throw new MyException("Vui lòng đăng nhập");
        Optional<Room> roomOptional = roomRepository.findById(roomId);
        if (roomOptional.isEmpty()) return null;
        var room = roomOptional.get();
        if (room.getType().equals(RoomType.ONE)) {
            var one = new RoomOneSummaryDto();
            one.setId(room.getId());
            one.setType(room.getType());
            if (room.getMembers() != null && room.getMembers().size() == 2) {
                var member = room.getMembers().stream()
                        .filter(x -> ! x.getUserId().equals(currentUser.getId()))
                        .findFirst();
                member.ifPresent(value -> one.setTo(userMapper.toUserProfileDto(value.getUserId())));
            }
            return one;
        } else {
            var group = new RoomGroupSummaryDto();
            group.setId(room.getId());
            group.setName(room.getName());
            group.setImageUrl(room.getImageUrl());
            group.setType(room.getType());
//            group.setMembers(room.getMembers());
            return group;
        }
    }
}
