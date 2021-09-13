package iuh.dhktpm14.cnm.chatappmongo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.MessageStatus;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Document
@Data
@Accessors(chain = true) //setter trả về this chứ không phải trả về void
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@CompoundIndexes(@CompoundIndex(background = true, def = "{roomId: 1, createAt: -1}"))
// do hay tìm kiếm dùng 2 trường này nên tạo index kết hợp trên cả hai trường
public class Message implements Serializable {
    @Id
    private String id;
    private String roomId;
    private String senderId;

    @CreatedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date createAt;
    private MessageType type;
    private String content;
    private Boolean pin;
    private List<Reaction> reactions;
    private MessageStatus status;
    private Boolean deleted;
    private String replyId;
}
