package iuh.dhktpm14.cnm.chatappmongo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InboxDto implements Serializable {
    private String id;
    private Object room;
    private MessageDto lastMessage;
    private long countNewMessage;
}
