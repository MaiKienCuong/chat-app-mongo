package iuh.dhktpm14.cnm.chatappmongo.mapper;

import iuh.dhktpm14.cnm.chatappmongo.dto.FriendDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.Friend;
import iuh.dhktpm14.cnm.chatappmongo.entity.FriendRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FriendMapper {

    @Autowired
    private UserMapper userMapper;

    public FriendDto toFriendDto(Friend friend) {
        if (friend == null)
            return null;
        var dto = new FriendDto();
        dto.setCreateAt(friend.getCreateAt());
        dto.setFriend(userMapper.toUserProfileDto(friend.getFriendId()));
        return dto;
    }

    public FriendDto toFriendRequest(FriendRequest friendRequest) {
        if (friendRequest == null)
            return null;
        var dto = new FriendDto();
        dto.setCreateAt(friendRequest.getCreateAt());
        dto.setFriend(userMapper.toUserProfileDto(friendRequest.getFromId()));
        return dto;
    }

}
