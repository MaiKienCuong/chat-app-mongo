package iuh.dhktpm14.cnm.chatappmongo.mapper;

import iuh.dhktpm14.cnm.chatappmongo.dto.FriendDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.FriendRequestReceivedDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.FriendRequestSentDto;
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

    public FriendRequestReceivedDto toFriendRequestReceived(FriendRequest friendRequest) {
        if (friendRequest == null)
            return null;
        var dto = new FriendRequestReceivedDto();
        dto.setCreateAt(friendRequest.getCreateAt());
        dto.setFrom(userMapper.toUserProfileDto(friendRequest.getFromId()));
        return dto;
    }

    public FriendRequestSentDto toFriendRequestSent(FriendRequest friendRequest) {
        if (friendRequest == null)
            return null;
        var dto = new FriendRequestSentDto();
        dto.setCreateAt(friendRequest.getCreateAt());
        dto.setTo(userMapper.toUserProfileDto(friendRequest.getToId()));
        return dto;
    }

}
