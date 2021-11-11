package iuh.dhktpm14.cnm.chatappmongo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Document
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
//@CompoundIndexes(value = { @CompoundIndex(background = true, def = "{fromId: 1, createAt: -1}"),
//        @CompoundIndex(background = true, def = "{toId: 1, createAt: -1}") })
public class FriendRequest implements Serializable {
    @Id
    private String id;
    @Indexed(background = true, direction = IndexDirection.ASCENDING)
    private String fromId;
    @Indexed(background = true, direction = IndexDirection.ASCENDING)
    private String toId;

    @CreatedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date createAt;
}
