package iuh.dhktpm14.cnm.chatappmongo.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadByFromClient implements Serializable {
    private String messageId;
    private String roomId;
    private String userId;
    private Date readAt;
}
