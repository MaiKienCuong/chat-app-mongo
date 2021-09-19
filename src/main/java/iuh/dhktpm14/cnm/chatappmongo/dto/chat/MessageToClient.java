package iuh.dhktpm14.cnm.chatappmongo.dto.chat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import iuh.dhktpm14.cnm.chatappmongo.dto.ReadByDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.UserProfileDto;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.MessageStatus;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.MessageType;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageToClient implements Serializable {
    private String id;
    private String roomId;
    private UserProfileDto sender;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date createAt;
    private MessageType type;
    private String content;
    private MessageStatus status;
    private List<ReadByDto> readbyes;
}
