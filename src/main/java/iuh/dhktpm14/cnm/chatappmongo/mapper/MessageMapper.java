package iuh.dhktpm14.cnm.chatappmongo.mapper;

import iuh.dhktpm14.cnm.chatappmongo.dto.MessageDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.Message;
import iuh.dhktpm14.cnm.chatappmongo.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MessageMapper {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserMapper userMapper;

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
        dto.setSender(userMapper.toUserProfileDto(message.getSenderId()));
        dto.setCreateAt(message.getCreateAt());
        dto.setType(message.getType());
        dto.setContent(message.getContent());
        dto.setPin(message.getPin());
        dto.setDeleted(message.getDeleted());
        dto.setStatus(message.getStatus());
        dto.setReactions(message.getReactions());
        return dto;
    }
}
