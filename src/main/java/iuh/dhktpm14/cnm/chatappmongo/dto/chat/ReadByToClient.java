package iuh.dhktpm14.cnm.chatappmongo.dto.chat;

import iuh.dhktpm14.cnm.chatappmongo.dto.UserProfileDto;
import lombok.Data;

import java.io.Serializable;

@Data
public class ReadByToClient implements Serializable {
    private String messageId;
    private String oldMessageId;
    private String roomId;
    private UserProfileDto readByUser;
    private String readAt;
}
