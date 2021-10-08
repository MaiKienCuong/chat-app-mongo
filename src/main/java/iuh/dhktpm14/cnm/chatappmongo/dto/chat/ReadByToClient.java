package iuh.dhktpm14.cnm.chatappmongo.dto.chat;

import iuh.dhktpm14.cnm.chatappmongo.dto.UserProfileDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadByToClient implements Serializable {
    private String messageId;
    private String oldMessageId;
    private String roomId;
    private UserProfileDto readByUser;
    private String readAt;
}
