package iuh.dhktpm14.cnm.chatappmongo.dto.chat;

import com.fasterxml.jackson.annotation.JsonInclude;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.MessageType;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageFromClient implements Serializable {
    private String roomId;
    private MessageType type;
    private String content;
    private String replyId;
}
