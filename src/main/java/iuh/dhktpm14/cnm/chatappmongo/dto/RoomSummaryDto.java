package iuh.dhktpm14.cnm.chatappmongo.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.RoomType;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoomSummaryDto implements Serializable {
    private String id;
    private String name;
    private RoomType type;
    private String imageUrl;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date createAt;

    private UserProfileDto to;
    private Set<MemberDto> members;
    private String createByUserId;
}
