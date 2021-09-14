package iuh.dhktpm14.cnm.chatappmongo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InboxDto implements Serializable, Comparable<InboxDto> {
    private String id;
    private Object room;
    private MessageDto lastMessage;
    private long countNewMessage;

    @Override
    public int compareTo(InboxDto o) {
        if (o != null && o.getLastMessage() != null && lastMessage != null)
            return o.getLastMessage().getCreateAt().compareTo(lastMessage.getCreateAt());
        return 0;
    }

}
