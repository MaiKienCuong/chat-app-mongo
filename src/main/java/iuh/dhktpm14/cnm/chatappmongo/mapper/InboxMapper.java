package iuh.dhktpm14.cnm.chatappmongo.mapper;

import iuh.dhktpm14.cnm.chatappmongo.dto.InboxDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.Inbox;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.UnAuthenticateException;
import iuh.dhktpm14.cnm.chatappmongo.repository.InboxRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class InboxMapper {
    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private InboxRepository inboxRepository;

    public InboxDto toInboxDto(String inboxId) {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user == null)
            throw new UnAuthenticateException();
        Optional<Inbox> inboxOptional = inboxRepository.findById(inboxId);
        if (inboxOptional.isEmpty()) return null;
        var inbox = inboxOptional.get();
        var dto = new InboxDto();
        dto.setId(inbox.getId());
        dto.setRoom(roomMapper.toRoomSummaryDto(inbox.getRoomId()));
        dto.setOfUserId(user.getId());
        dto.setCountNewMessage(messageRepository.countNewMessage(inbox.getRoomId(), user.getId()));
        dto.setLastMessage(messageMapper.toMessageDto(messageRepository.findLastMessageByRoomId(inbox.getRoomId())));
        return dto;
    }

    public InboxDto toInboxDto(Inbox inbox) {
        if (inbox == null)
            return null;
        var dto = new InboxDto();
        dto.setId(inbox.getId());
        dto.setRoom(roomMapper.toRoomSummaryDto(inbox.getRoomId()));
        dto.setOfUserId(inbox.getOfUserId());
        dto.setCountNewMessage(messageRepository.countNewMessage(inbox.getRoomId(), inbox.getOfUserId()));
        dto.setLastMessage(messageMapper.toMessageDto(messageRepository.findLastMessageByRoomId(inbox.getRoomId())));
        return dto;
    }
}
