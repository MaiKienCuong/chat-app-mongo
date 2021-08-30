package iuh.dhktpm14.cnm.chatappmongo.mapper;

import iuh.dhktpm14.cnm.chatappmongo.dto.InboxDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.ReadByDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.Inbox;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.UnAuthenticateException;
import iuh.dhktpm14.cnm.chatappmongo.repository.InboxRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class InboxMapper {
    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private ReadByMapper readByMapper;

    @Autowired
    private InboxRepository inboxRepository;

    public InboxDto toInboxDto(String inboxId) {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user == null)
            throw new UnAuthenticateException();
        if (inboxId == null)
            return null;
        Optional<Inbox> inboxOptional = inboxRepository.findById(inboxId);
        if (inboxOptional.isEmpty())
            return null;
        return toInboxDto(inboxOptional.get());
    }

    public InboxDto toInboxDto(Inbox inbox) {
        if (inbox == null)
            return null;
        var dto = new InboxDto();
        dto.setId(inbox.getId());
        dto.setRoom(roomMapper.toRoomSummaryDto(inbox.getRoomId()));
        dto.setCountNewMessage(messageRepository.countNewMessage(inbox.getRoomId(), inbox.getOfUserId()));
        var lastMessage = messageRepository.getLastMessageOfRoom(inbox.getRoomId());
        if (lastMessage != null) {
            dto.setLastMessage(messageMapper.toMessageDto(lastMessage));
            if (lastMessage.getReadByes() != null) {
                dto.setLastMessageReadBy(lastMessage.getReadByes()
                        .stream().map(x -> readByMapper.toReadByDto(x))
                        .sorted(Comparator.comparing(ReadByDto::getReadAt))
                        .collect(Collectors.toCollection(LinkedHashSet::new)));
            }
        }
        return dto;
    }
}
