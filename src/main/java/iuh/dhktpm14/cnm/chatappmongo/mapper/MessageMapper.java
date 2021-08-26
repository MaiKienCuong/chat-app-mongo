package iuh.dhktpm14.cnm.chatappmongo.mapper;

import iuh.dhktpm14.cnm.chatappmongo.dto.MessageDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.Message;
import iuh.dhktpm14.cnm.chatappmongo.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    public MessageDto toMessageDto(String messageId) {
        var dto = new MessageDto();
        Optional<Message> messageOptional = messageRepository.findById(messageId);
        if (messageOptional.isEmpty()) return null;
        var message = messageOptional.get();
        dto.setId(message.getId());
        dto.setSender(userMapper.toUserProfileDto(message.getSenderId()));
        dto.setCreateAt(message.getCreateAt());
        dto.setType(message.getType());
        dto.setContent(message.getContent());
        dto.setPin(message.getPin());
        dto.setDeleted(message.getDeleted());
        dto.setStatus(message.getStatus());
        if (message.getReadByes() != null)
            dto.setReadByes(message.getReadByes().stream().map(readByMapper::toReadByDto).collect(Collectors.toList()));
        dto.setReactions(message.getReactions());
        return dto;
    }

    public MessageDto toMessageDto(Message message) {
        var dto = new MessageDto();
        if (message == null) return null;
        dto.setId(message.getId());
        dto.setSender(userMapper.toUserProfileDto(message.getSenderId()));
        dto.setCreateAt(message.getCreateAt());
        dto.setType(message.getType());
        dto.setContent(message.getContent());
        dto.setPin(message.getPin());
        dto.setDeleted(message.getDeleted());
        dto.setStatus(message.getStatus());
        if (message.getReadByes() != null)
            dto.setReadByes(message.getReadByes().stream().map(readByMapper::toReadByDto).collect(Collectors.toList()));
        dto.setReactions(message.getReactions());
        return dto;
    }
}
