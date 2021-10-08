package iuh.dhktpm14.cnm.chatappmongo.dto.chat;

import iuh.dhktpm14.cnm.chatappmongo.dto.UserProfileDto;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.ReactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReactionToClient implements Serializable {
    private String messageId;
    private String roomId;
    private UserProfileDto reactByUser;
    private ReactionType type;
}
