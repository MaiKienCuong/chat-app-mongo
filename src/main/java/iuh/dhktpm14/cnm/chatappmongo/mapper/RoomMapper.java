package iuh.dhktpm14.cnm.chatappmongo.mapper;

import iuh.dhktpm14.cnm.chatappmongo.dto.RoomGroupDetailDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.RoomGroupSummaryDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.RoomOneDetailDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.RoomOneSummaryDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.Member;
import iuh.dhktpm14.cnm.chatappmongo.entity.Room;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.RoomType;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.UnAuthenticateException;
import iuh.dhktpm14.cnm.chatappmongo.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RoomMapper {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MemberMapper memberMapper;

    public Object toRoomSummaryDto(String roomId) {
        var currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (currentUser == null)
            throw new UnAuthenticateException();
        if (roomId == null)
            return null;
        Optional<Room> roomOptional = roomRepository.findById(roomId);
        if (roomOptional.isEmpty())
            return null;
        return toRoomSummaryDto(roomOptional.get());
    }

    public Object toRoomSummaryDto(Room room) {
        var currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (currentUser == null)
            throw new UnAuthenticateException();
        if (room == null)
            return null;
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
            if (room.getMembers() != null)
                group.setNumOfMembers(room.getMembers().size());
            return group;
        }
    }

    public Object toRoomDetailDto(String roomId) {
        var currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (currentUser == null)
            throw new UnAuthenticateException();
        if (roomId == null)
            return null;
        Optional<Room> roomOptional = roomRepository.findById(roomId);
        if (roomOptional.isEmpty())
            return null;
        return toRoomDetailDto(roomOptional.get());
    }

    public Object toRoomDetailDto(Room room) {
        var currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (currentUser == null)
            throw new UnAuthenticateException();
        if (room == null)
            return null;
        if (room.getType().equals(RoomType.ONE)) {
            var one = new RoomOneDetailDto();
            one.setId(room.getId());
            one.setType(room.getType());
            one.setCreateAt(room.getCreateAt());
            one.setCreateByUser(userMapper.toUserProfileDto(room.getCreateByUserId()));
            if (room.getMembers() != null && room.getMembers().size() == 2) {
                var member = room.getMembers().stream()
                        .filter(x -> ! x.getUserId().equals(currentUser.getId()))
                        .findFirst();
                member.ifPresent(value -> one.setTo(userMapper.toUserProfileDto(value.getUserId())));
            }
            return one;
        } else {
            var group = new RoomGroupDetailDto();
            group.setId(room.getId());
            group.setName(room.getName());
            group.setImageUrl(room.getImageUrl());
            group.setType(room.getType());
            group.setCreateAt(room.getCreateAt());
            group.setCreateByUser(userMapper.toUserProfileDto(room.getCreateByUserId()));
            Set<Member> members = room.getMembers();
            group.setMembers(members.stream().map(x -> memberMapper.toMemberDto(x)).collect(Collectors.toSet()));
            return group;
        }
    }
}
