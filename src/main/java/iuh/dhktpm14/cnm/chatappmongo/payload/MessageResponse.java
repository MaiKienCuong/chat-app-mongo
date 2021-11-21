package iuh.dhktpm14.cnm.chatappmongo.payload;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessageResponse {
    private String field;
    private String message;

    public MessageResponse(String message) {
        this.message = message;
    }
}
