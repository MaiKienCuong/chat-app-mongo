package iuh.dhktpm14.cnm.chatappmongo.service;

import iuh.dhktpm14.cnm.chatappmongo.dto.FriendDeleteDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FriendSocketService {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public boolean sendDeleteFriendMessage(String currentUserId, String friendDeletedId) {
        if (friendDeletedId == null)
            return false;
        var friendDeleteDto = FriendDeleteDto.builder()
                .userId(currentUserId)
                .friendId(friendDeletedId)
                .build();
        if (friendDeleteDto != null) {
            messagingTemplate.convertAndSendToUser(currentUserId, "/queue/friends/delete",
                    friendDeleteDto);

            messagingTemplate.convertAndSendToUser(friendDeletedId, "/queue/friends/delete",
                    friendDeleteDto);
            return true;
        }
        return false;
    }

}
