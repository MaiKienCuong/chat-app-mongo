package iuh.dhktpm14.cnm.chatappmongo.mapper;

import iuh.dhktpm14.cnm.chatappmongo.dto.RoomDetailDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.RoomSummaryDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.Member;
import iuh.dhktpm14.cnm.chatappmongo.entity.Room;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.RoomType;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.MyException;
import iuh.dhktpm14.cnm.chatappmongo.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RoomMapper {

    @Autowired
    private RoomService roomService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private MessageSource messageSource;

    public RoomSummaryDto toRoomSummaryDto(String roomId) {
        authenticate();
        if (roomId == null)
            return null;
        Optional<Room> roomOptional = roomService.findById(roomId);
        return toRoomSummaryDto(roomOptional.orElse(null));
    }

    public RoomSummaryDto toRoomSummaryDto(Room room) {
        var currentUser = authenticate();
        if (room == null)
            return null;
        var result = new RoomSummaryDto();
        result.setId(room.getId());
        result.setType(room.getType());
        if (room.getType().equals(RoomType.ONE)) {
            if (room.getMembers() != null && room.getMembers().size() == 2) {
                var member = room.getMembers().stream()
                        .filter(x -> ! x.getUserId().equals(currentUser.getId()))
                        .findFirst();
                member.ifPresent(value -> result.setTo(userMapper.toUserProfileDto(value.getUserId())));
            }
        } else {
            result.setName(room.getName());
            result.setImageUrl(room.getImageUrl());
            result.setCreateAt(room.getCreateAt());
            result.setCreateByUserId(room.getCreateByUserId());
            Set<Member> members = room.getMembers();
            if (members != null)
                result.setMembers(members.stream().map(x -> memberMapper.toMemberDto(x)).sorted().collect(Collectors.toCollection(LinkedHashSet::new)));
            else
                result.setMembers(new HashSet<>(0));
        }
        return result;
    }

    public RoomDetailDto toRoomDetailDto(String roomId) {
        authenticate();
        if (roomId == null)
            return null;
        Optional<Room> roomOptional = roomService.findById(roomId);
        return toRoomDetailDto(roomOptional.orElse(null));
    }

    public RoomDetailDto toRoomDetailDto(Room room) {
        var currentUser = authenticate();
        if (room == null)
            return null;
        var result = new RoomDetailDto();
        result.setId(room.getId());
        result.setType(room.getType());
        result.setCreateAt(room.getCreateAt());
        result.setCreateByUser(userMapper.toUserProfileDto(room.getCreateByUserId()));
        if (room.getType().equals(RoomType.ONE)) {
            if (room.getMembers() != null && room.getMembers().size() == 2) {
                var member = room.getMembers().stream()
                        .filter(x -> ! x.getUserId().equals(currentUser.getId()))
                        .findFirst();
                member.ifPresent(value -> result.setTo(userMapper.toUserProfileDto(value.getUserId())));
            }
        } else {
            result.setName(room.getName());
            result.setImageUrl(room.getImageUrl());
            Set<Member> members = room.getMembers();
            if (members != null)
                result.setMembers(members.stream().map(x -> memberMapper.toMemberDto(x)).sorted().collect(Collectors.toCollection(LinkedHashSet::new)));
            else
                result.setMembers(new HashSet<>(0));
        }
        return result;
    }

    private User authenticate() {
        var currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (currentUser == null) {
            String message = messageSource.getMessage("unauthorized", null, LocaleContextHolder.getLocale());
            throw new MyException(message);
        }
        return currentUser;
    }

}
