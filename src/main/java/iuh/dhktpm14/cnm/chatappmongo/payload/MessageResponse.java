package iuh.dhktpm14.cnm.chatappmongo.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageResponse {
    private String field;
    private String message;

    public MessageResponse(String message) {
        this.message = message;
    }
}
