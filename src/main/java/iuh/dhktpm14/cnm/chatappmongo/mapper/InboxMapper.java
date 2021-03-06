package iuh.dhktpm14.cnm.chatappmongo.mapper;

import iuh.dhktpm14.cnm.chatappmongo.dto.InboxDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.Inbox;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.UnAuthenticateException;
import iuh.dhktpm14.cnm.chatappmongo.repository.InboxRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.MessageRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.ReadTrackingRepository;
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

    @Autowired
    private ReadTrackingRepository readTrackingRepository;

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
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user == null)
            throw new UnAuthenticateException();
        if (inbox == null)
            return null;
        var dto = new InboxDto();
        dto.setId(inbox.getId());
        dto.setRoom(roomMapper.toRoomSummaryDto(inbox.getRoomId()));
        /*
        lấy số tin nhắn chưa đọc theo roomId và userId
         */
        var readTracking = readTrackingRepository.findByRoomIdAndUserId(inbox.getRoomId(), user.getId());
        if (readTracking != null)
            dto.setCountNewMessage(readTracking.getUnReadMessage());
        var lastMessage = messageRepository.getLastMessageOfRoom(inbox.getRoomId());
        dto.setLastMessage(messageMapper.toMessageDto(lastMessage));
        return dto;
    }
}
