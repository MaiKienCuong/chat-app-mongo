package iuh.dhktpm14.cnm.chatappmongo.dto.chat;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ReadByFromClient implements Serializable {
    private String messageId;
    private String roomId;
    private String userId;
    private Date readAt;
}
