package iuh.dhktpm14.cnm.chatappmongo.mapper;

import iuh.dhktpm14.cnm.chatappmongo.dto.InboxDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.InboxSummaryDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.Inbox;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.MyException;
import iuh.dhktpm14.cnm.chatappmongo.service.InboxService;
import iuh.dhktpm14.cnm.chatappmongo.service.MessageService;
import iuh.dhktpm14.cnm.chatappmongo.service.ReadTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class InboxMapper {
    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private InboxService inboxService;

    @Autowired
    private ReadTrackingService readTrackingService;

    @Autowired
    private MessageSource messageSource;

    public InboxDto toInboxDto(String inboxId) {
        authenticate();
        if (inboxId == null)
            return null;
        Optional<Inbox> inboxOptional = inboxService.findById(inboxId);
        return toInboxDto(inboxOptional.orElse(null));
    }

    public InboxDto toInboxDto(Inbox inbox) {
        var user = authenticate();
        if (inbox == null)
            return null;
        var dto = new InboxDto();
        dto.setId(inbox.getId());
        dto.setRoom(roomMapper.toRoomSummaryDto(inbox.getRoomId()));
        /*
        lấy số tin nhắn chưa đọc theo roomId và userId
         */
        var readTracking = readTrackingService.findByRoomIdAndUserId(inbox.getRoomId(), user.getId());
        if (readTracking != null)
            dto.setCountNewMessage(readTracking.getUnReadMessage());
        var lastMessage = messageService.getLastMessageOfRoom(user.getId(), inbox.getRoomId());
        lastMessage.ifPresent(message -> dto.setLastMessage(messageMapper.toMessageDto(message)));
        dto.setLastTime(inbox.getLastTime());
        return dto;
    }

    public InboxSummaryDto toInboxSummaryDto(Inbox inbox) {
        authenticate();
        if (inbox == null)
            return null;
        var dto = new InboxSummaryDto();
        dto.setId(inbox.getId());
        dto.setRoom(roomMapper.toRoomSummaryDto(inbox.getRoomId()));
        return dto;
    }

    private User authenticate() {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user == null) {
            String message = messageSource.getMessage("unauthorized", null, LocaleContextHolder.getLocale());
            throw new MyException(message);
        }
        return user;
    }
}
