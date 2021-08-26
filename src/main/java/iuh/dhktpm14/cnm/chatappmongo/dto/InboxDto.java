package iuh.dhktpm14.cnm.chatappmongo.dto;

import lombok.Data;

@Data
public class InboxDto {
    private String id;
    private String ofUserId;
    private Object room;
    private MessageDto lastMessage;
    private Long countNewMessage;
}
