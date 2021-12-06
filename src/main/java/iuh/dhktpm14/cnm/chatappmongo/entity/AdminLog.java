package iuh.dhktpm14.cnm.chatappmongo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Document
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminLog implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 9046282216797032360L;

    @Id
    private String id;
    private String content;
    private String relatedObjectId;
    private String handlerObjectId;

    @CreatedDate
    private Date time;

}
