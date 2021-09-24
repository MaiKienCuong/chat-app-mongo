package iuh.dhktpm14.cnm.chatappmongo.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.RoomType;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoomGroupSummaryDto implements Serializable {
    private String id;
    private String name;
    private String imageUrl;
    private RoomType type;
    private int numOfMembers;
}
