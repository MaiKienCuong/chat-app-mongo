package iuh.dhktpm14.cnm.chatappmongo.dto.chat;

import com.fasterxml.jackson.annotation.JsonFormat;
import iuh.dhktpm14.cnm.chatappmongo.dto.UserProfileDto;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ReadByToClient implements Serializable {
    private String messageId;
    private String roomId;
    private UserProfileDto readByUser;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date readAt;
}
