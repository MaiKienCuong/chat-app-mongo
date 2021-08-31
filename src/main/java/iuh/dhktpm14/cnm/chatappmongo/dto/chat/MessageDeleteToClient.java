package iuh.dhktpm14.cnm.chatappmongo.dto.chat;

import lombok.Data;

import java.io.Serializable;

@Data
public class MessageDeleteToClient implements Serializable {
    private String messageId;
    private String roomId;
    private String content;
}
