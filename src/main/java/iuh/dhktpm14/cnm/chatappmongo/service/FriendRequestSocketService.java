package iuh.dhktpm14.cnm.chatappmongo.service;

import iuh.dhktpm14.cnm.chatappmongo.entity.FriendRequest;
import iuh.dhktpm14.cnm.chatappmongo.mapper.FriendMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FriendRequestSocketService {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private FriendMapper friendMapper;

    public boolean sendFriendRequestReceived(FriendRequest friendRequest) {
        if (! valid(friendRequest))
            return false;
        log.info("sending friend request received from userId = {}, to userId = {}", friendRequest.getFromId(), friendRequest.getToId());
        messagingTemplate.convertAndSendToUser(friendRequest.getToId(), "/queue/friendRequest/received",
                friendMapper.toFriendRequestDto(friendRequest));

        messagingTemplate.convertAndSendToUser(friendRequest.getFromId(), "/queue/friendRequest/received",
                friendMapper.toFriendRequestDto(friendRequest));
        return true;
    }

    public boolean sendFriendRequestAccept(FriendRequest friendRequest) {
        if (! valid(friendRequest))
            return false;
        log.info("sending friend request accept from userId = {}, to userId = {}", friendRequest.getToId(), friendRequest.getFromId());
        messagingTemplate.convertAndSendToUser(friendRequest.getFromId(), "/queue/friendRequest/accept",
                friendMapper.toFriendRequestDto(friendRequest));

        messagingTemplate.convertAndSendToUser(friendRequest.getToId(), "/queue/friendRequest/accept",
                friendMapper.toFriendRequestDto(friendRequest));
        return true;
    }

    public boolean sendFriendRequestRecall(FriendRequest friendRequest) {
        if (! valid(friendRequest))
            return false;
        log.info("sending friend request recall from userId = {}, to userId = {}", friendRequest.getFromId(), friendRequest.getToId());
        messagingTemplate.convertAndSendToUser(friendRequest.getToId(), "/queue/friendRequest/recall",
                friendMapper.toFriendRequestDto(friendRequest));

        messagingTemplate.convertAndSendToUser(friendRequest.getFromId(), "/queue/friendRequest/recall",
                friendMapper.toFriendRequestDto(friendRequest));
        return true;
    }

    public boolean sendFriendRequestDelete(FriendRequest friendRequest) {
        if (! valid(friendRequest))
            return false;
        log.info("sending friend request delete from userId = {}, to userId = {}", friendRequest.getFromId(), friendRequest.getToId());
        messagingTemplate.convertAndSendToUser(friendRequest.getFromId(), "/queue/friendRequest/delete",
                friendMapper.toFriendRequestDto(friendRequest));

        messagingTemplate.convertAndSendToUser(friendRequest.getToId(), "/queue/friendRequest/delete",
                friendMapper.toFriendRequestDto(friendRequest));
        return true;
    }

    private boolean valid(FriendRequest friendRequest) {
        if (friendRequest == null) {
            log.error("friend request is null");
            return false;
        }
        if (friendRequest.getId() == null) {
            log.error("friend request id is null");
            return false;
        }
        if (friendRequest.getFromId() == null) {
            log.error("friend request from id is null");
            return false;
        }
        if (friendRequest.getToId() == null) {
            log.error("friend request to id is null");
            return false;
        }
        return true;
    }

}
