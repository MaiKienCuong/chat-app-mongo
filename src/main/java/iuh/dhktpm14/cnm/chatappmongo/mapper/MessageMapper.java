package iuh.dhktpm14.cnm.chatappmongo.mapper;

import iuh.dhktpm14.cnm.chatappmongo.dto.MessageDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.ReadByDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.chat.MessageToClient;
import iuh.dhktpm14.cnm.chatappmongo.entity.Message;
import iuh.dhktpm14.cnm.chatappmongo.entity.Reaction;
import iuh.dhktpm14.cnm.chatappmongo.entity.ReadTracking;
import iuh.dhktpm14.cnm.chatappmongo.repository.MessageRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.ReadTrackingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MessageMapper {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ReadByMapper readByMapper;

    @Autowired
    private ReadTrackingRepository readTrackingRepository;

    public MessageDto toMessageDto(String messageId) {
        if (messageId == null)
            return null;
        Optional<Message> messageOptional = messageRepository.findById(messageId);
        if (messageOptional.isEmpty())
            return null;

        return toMessageDto(messageOptional.get());
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
            Optional<Message> optional = messageRepository.findById(message.getReplyId());
            optional.ifPresent(value -> dto.setReply(toMessageDto(value)));
        }
        /*
        lấy danh sách người đã đọc tin nhắn này
         */
        List<ReadTracking> readTracking = readTrackingRepository.findAllByMessageId(message.getId());
        List<ReadByDto> readBy = readTracking.stream().map(readByMapper::toReadByDto).collect(Collectors.toList());
        dto.setReadbyes(readBy);
        return dto;
    }

    public MessageToClient toMessageToClient(Message message) {
        if (message == null)
            return null;
        var dto = new MessageToClient();
        dto.setId(message.getId());
        if (message.getSenderId() != null)
            dto.setSender(userMapper.toUserProfileDto(message.getSenderId()));
        dto.setCreateAt(message.getCreateAt());
        dto.setType(message.getType());
        dto.setContent(message.getContent());
        dto.setStatus(message.getStatus());
        dto.setRoomId(message.getRoomId());
        List<ReadTracking> readTracking = readTrackingRepository.findAllByMessageId(message.getId());
        List<ReadByDto> readBy = readTracking.stream().map(readByMapper::toReadByDto).collect(Collectors.toList());
        dto.setReadbyes(readBy);
        return dto;
    }
}
