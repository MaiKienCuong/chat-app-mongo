package iuh.dhktpm14.cnm.chatappmongo.dto.chat;

import iuh.dhktpm14.cnm.chatappmongo.enumvalue.ReactionType;
import lombok.Data;

import java.io.Serializable;

@Data
public class ReactionFromClient implements Serializable {
    private String messageId;
    private String roomId;
    private String userId;
    private ReactionType type;
}
