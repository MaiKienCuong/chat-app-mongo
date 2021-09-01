package iuh.dhktpm14.cnm.chatappmongo.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.RoomType;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoomOneDetailDto implements Serializable {
    private String id;
    private RoomType type;
    private UserProfileDto to;
    private Date createAt;
    private Set<MemberDto> members;
    private UserProfileDto createByUser;
}
