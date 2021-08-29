package iuh.dhktpm14.cnm.chatappmongo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InboxDto implements Serializable {
    private String id;
    private Object room;
    private MessageDto lastMessage;
    private Set<ReadByDto> lastMessageReadBy;
    private Long countNewMessage;
}
