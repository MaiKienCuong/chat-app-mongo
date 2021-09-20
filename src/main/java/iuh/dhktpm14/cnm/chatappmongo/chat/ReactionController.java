package iuh.dhktpm14.cnm.chatappmongo.chat;

import iuh.dhktpm14.cnm.chatappmongo.dto.chat.ReactionFromClient;
import iuh.dhktpm14.cnm.chatappmongo.dto.chat.ReactionToClient;
import iuh.dhktpm14.cnm.chatappmongo.entity.Member;
import iuh.dhktpm14.cnm.chatappmongo.entity.Reaction;
import iuh.dhktpm14.cnm.chatappmongo.entity.Room;
import iuh.dhktpm14.cnm.chatappmongo.mapper.UserMapper;
import iuh.dhktpm14.cnm.chatappmongo.repository.RoomRepository;
import iuh.dhktpm14.cnm.chatappmongo.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Optional;

@Controller
public class ReactionController {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MessageService messageService;

    @MessageMapping("/reaction")
    public void processMessage(@Payload ReactionFromClient reaction, UserPrincipal userPrincipal) {
        System.out.println("reaction = " + reaction);

        var reactionToClient = new ReactionToClient();
        reactionToClient.setMessageId(reaction.getMessageId());
        reactionToClient.setRoomId(reaction.getRoomId());
        reactionToClient.setType(reaction.getType());
        reactionToClient.setReactByUser(userMapper.toUserProfileDto(reaction.getUserId()));

        var react = new Reaction();
        react.setType(reaction.getType());
        react.setReactByUserId(reaction.getUserId());

        messageService.addReactToMessage(reaction.getMessageId(), react);

        Optional<Room> roomOptional = roomRepository.findById(reaction.getRoomId());
        if (roomOptional.isPresent()) {
            var room = roomOptional.get();
            for (Member member : room.getMembers()) {
                messagingTemplate.convertAndSendToUser(member.getUserId(), "/queue/reaction", reactionToClient);
            }
        }
    }

}
