package iuh.dhktpm14.cnm.chatappmongo.dto.chat;

import iuh.dhktpm14.cnm.chatappmongo.dto.UserProfileDto;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.ReactionType;
import lombok.Data;

import java.io.Serializable;

@Data
public class ReactionToClient implements Serializable {
    private String messageId;
    private String roomId;
    private UserProfileDto reactByUser;
    private ReactionType type;
}
