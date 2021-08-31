package iuh.dhktpm14.cnm.chatappmongo.chat;

//import iuh.dhktpm14.cnm.chatappmongo.dto.MessageCreateDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.MessageDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.Member;
import iuh.dhktpm14.cnm.chatappmongo.entity.Room;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.mapper.UserMapper;
import iuh.dhktpm14.cnm.chatappmongo.repository.InboxRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.MessageRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.RoomRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

@Controller
public class ChatController {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private InboxRepository inboxRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    /*@MessageMapping("/chat")
    public void processMessage(@Payload MessageCreateDto messageDto, UserPrincipal userPrincipal) {
        System.out.println("access_token = " + userPrincipal.getAccessToken());
        Optional<Room> roomOptional = roomRepository.findById(messageDto.getRoomId());
        Optional<User> userOptional = userRepository.findById(userPrincipal.getName());
        if (roomOptional.isPresent() && userOptional.isPresent()) {
            var room = roomOptional.get();
            var user = userOptional.get();
            Set<Member> members = room.getMembers();
            String content = messageDto.getContent();
            for (Member m : members) {
                if (! m.getUserId().equals(user.getId())) {
                    var dto = new MessageDto();
                    dto.setCreateAt(new Date());
                    dto.setSender(userMapper.toUserProfileDto(user));
                    dto.setContent(content);
                    messagingTemplate.convertAndSendToUser(m.getUserId(), "/queue/messages", dto);
                }
            }
        }*/
//    }

}
