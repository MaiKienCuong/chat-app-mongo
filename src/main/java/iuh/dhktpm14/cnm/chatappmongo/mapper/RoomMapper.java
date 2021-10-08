package iuh.dhktpm14.cnm.chatappmongo.mapper;

import iuh.dhktpm14.cnm.chatappmongo.dto.RoomDetailDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.RoomSummaryDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.Member;
import iuh.dhktpm14.cnm.chatappmongo.entity.Room;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.RoomType;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.UnAuthenticateException;
import iuh.dhktpm14.cnm.chatappmongo.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
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

    public RoomSummaryDto toRoomSummaryDto(String roomId) {
        var currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (currentUser == null)
            throw new UnAuthenticateException();
        if (roomId == null)
            return null;
        Optional<Room> roomOptional = roomService.findById(roomId);
        if (roomOptional.isEmpty())
            return null;
        return toRoomSummaryDto(roomOptional.get());
    }

    public RoomSummaryDto toRoomSummaryDto(Room room) {
        var currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (currentUser == null)
            throw new UnAuthenticateException();
        if (room == null)
            return null;
        var result = new RoomSummaryDto();
        if (room.getType().equals(RoomType.ONE)) {
            result.setId(room.getId());
            result.setType(room.getType());
            if (room.getMembers() != null && room.getMembers().size() == 2) {
                var member = room.getMembers().stream()
                        .filter(x -> ! x.getUserId().equals(currentUser.getId()))
                        .findFirst();
                member.ifPresent(value -> result.setTo(userMapper.toUserProfileDto(value.getUserId())));
            }
            return result;
        } else {
            result.setId(room.getId());
            result.setName(room.getName());
            result.setImageUrl(room.getImageUrl());
            result.setType(room.getType());
            result.setCreateAt(room.getCreateAt());
            result.setCreateByUserId(room.getCreateByUserId());
            Set<Member> members = room.getMembers();
            if (members != null)
                result.setMembers(members);
            else
                result.setMembers(new HashSet<>(0));
            return result;
        }
    }

    public RoomDetailDto toRoomDetailDto(String roomId) {
        var currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (currentUser == null)
            throw new UnAuthenticateException();
        if (roomId == null)
            return null;
        Optional<Room> roomOptional = roomService.findById(roomId);
        if (roomOptional.isEmpty())
            return null;
        return toRoomDetailDto(roomOptional.get());
    }

    public RoomDetailDto toRoomDetailDto(Room room) {
        var currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (currentUser == null)
            throw new UnAuthenticateException();
        if (room == null)
            return null;
        var result = new RoomDetailDto();
        if (room.getType().equals(RoomType.ONE)) {
            result.setId(room.getId());
            result.setType(room.getType());
            result.setCreateAt(room.getCreateAt());
            result.setCreateByUser(userMapper.toUserProfileDto(room.getCreateByUserId()));
            if (room.getMembers() != null && room.getMembers().size() == 2) {
                var member = room.getMembers().stream()
                        .filter(x -> ! x.getUserId().equals(currentUser.getId()))
                        .findFirst();
                member.ifPresent(value -> result.setTo(userMapper.toUserProfileDto(value.getUserId())));
            }
            return result;
        } else {
            result.setId(room.getId());
            result.setName(room.getName());
            result.setImageUrl(room.getImageUrl());
            result.setType(room.getType());
            result.setCreateAt(room.getCreateAt());
            result.setCreateByUser(userMapper.toUserProfileDto(room.getCreateByUserId()));
            Set<Member> members = room.getMembers();
            if (members != null)
                result.setMembers(members.stream().map(x -> memberMapper.toMemberDto(x)).collect(Collectors.toSet()));
            else
                result.setMembers(new HashSet<>(0));
            return result;
        }
    }
}
