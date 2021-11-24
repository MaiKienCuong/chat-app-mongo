package iuh.dhktpm14.cnm.chatappmongo.mapper;

import iuh.dhktpm14.cnm.chatappmongo.dto.MessageDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.ReadByDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.chat.MessageToClient;
import iuh.dhktpm14.cnm.chatappmongo.entity.Message;
import iuh.dhktpm14.cnm.chatappmongo.entity.Reaction;
import iuh.dhktpm14.cnm.chatappmongo.entity.ReadTracking;
import iuh.dhktpm14.cnm.chatappmongo.service.MessageService;
import iuh.dhktpm14.cnm.chatappmongo.service.ReadTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MessageMapper {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ReadByMapper readByMapper;

    @Autowired
    private ReadTrackingService readTrackingService;

    public MessageDto toMessageDto(String messageId) {
        if (messageId == null)
            return null;
        Optional<Message> messageOptional = messageService.findById(messageId);
        return toMessageDto(messageOptional.orElse(null));
    }

    public MessageDto toMessageDto(Message message) {
        if (message == null)
            return null;
        var dto = new MessageDto();
        dto.setId(message.getId());
        if (message.getSenderId() != null)
            dto.setSender(userMapper.toUserProfileDto(message.getSenderId()));
        dto.setCreateAt(message.getCreateAt());
        dto.setType(message.getType());
        dto.setContent(message.getContent());
        dto.setPin(message.isPin());
        dto.setDeleted(message.isDeleted());
        dto.setStatus(message.getStatus());
        List<Reaction> reactions = message.getReactions();
        if (reactions == null)
            dto.setReactions(new ArrayList<>(0));
        else {
            Collections.reverse(reactions);
            dto.setReactions(reactions);
        }
        dto.setRoomId(message.getRoomId());
        if (message.getReplyId() != null) {
            dto.setReply(toMessageDto(message.getReplyId()));
        }
        /*
        lấy danh sách người đã đọc tin nhắn này
         */
        List<ReadTracking> readTracking = readTrackingService.findAllByMessageId(message.getId());
        List<ReadByDto> readBy = readTracking.stream().map(readByMapper::toReadByDto).collect(Collectors.toList());
        dto.setReadbyes(readBy);
        dto.setMedia(message.getMedia());
        return dto;
    }

    public MessageToClient toMessageToClient(Message message) {
        if (message == null)
            return null;
        var dto = new MessageToClient();
        dto.setId(message.getId());
        if (message.getSenderId() != null) {
            var senderProfile = userMapper.toUserProfileDto(message.getSenderId());
            dto.setSender(senderProfile);
            List<ReadByDto> list = new ArrayList<>();
            list.add(ReadByDto.builder().readAt(new Date()).readByUser(senderProfile).build());
            dto.setReadbyes(list);
        }
        dto.setCreateAt(message.getCreateAt());
        dto.setType(message.getType());
        dto.setContent(message.getContent());
        dto.setPin(message.isPin());
        dto.setDeleted(message.isDeleted());
        dto.setStatus(message.getStatus());
        dto.setRoomId(message.getRoomId());
        if (message.getReplyId() != null) {
            Optional<Message> optional = messageService.findById(message.getReplyId());
            optional.ifPresent(value -> dto.setReply(toMessageToClient(value)));
        }
//        List<ReadTracking> readTracking = readTrackingRepository.findAllByMessageId(message.getId());
//        List<ReadByDto> readBy = readTracking.stream().map(readByMapper::toReadByDto).collect(Collectors.toList());
//        dto.setReadbyes(readBy);
        dto.setMedia(message.getMedia());
        return dto;
    }
}
