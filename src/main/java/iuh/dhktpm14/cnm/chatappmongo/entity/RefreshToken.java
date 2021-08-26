package iuh.dhktpm14.cnm.chatappmongo.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document
public class RefreshToken {

    @Id
    private String id;
    private String userId;
    private String token;
    private Instant expiredTime;

}
