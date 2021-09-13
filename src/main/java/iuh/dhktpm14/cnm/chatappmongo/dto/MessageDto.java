package iuh.dhktpm14.cnm.chatappmongo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import iuh.dhktpm14.cnm.chatappmongo.entity.Reaction;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.MessageStatus;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.MessageType;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageDto implements Serializable {
    private String id;
    private UserProfileDto sender;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date createAt;
    private MessageType type;
    private String content;
    private Boolean pin;
    private Boolean deleted;
    private MessageStatus status;
    private List<Reaction> reactions;
    private List<ReadByDto> readbyes;
    private MessageDto reply;
}
