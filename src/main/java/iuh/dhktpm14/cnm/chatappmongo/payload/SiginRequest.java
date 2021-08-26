package iuh.dhktpm14.cnm.chatappmongo.payload;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SiginRequest {

    @JsonAlias(value = { "phone", "email" })
    private String username;
    private String password;

}
