package iuh.dhktpm14.cnm.chatappmongo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageFromClient implements Serializable {
    private String roomId;
    private String type;
    private String content;
}
