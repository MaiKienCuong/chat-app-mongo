package iuh.dhktpm14.cnm.chatappmongo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InboxDto implements Serializable, Comparable<InboxDto> {
    private String id;
    private RoomSummaryDto room;
    private MessageDto lastMessage;
    private long countNewMessage;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date lastTime;

    @Override
    public int compareTo(InboxDto o) {
        if (lastTime == null)
            return 0;
        if (o == null)
            return 0;
        if (o.getLastTime() == null)
            return 0;
        return o.getLastTime().compareTo(lastTime);
    }

}
