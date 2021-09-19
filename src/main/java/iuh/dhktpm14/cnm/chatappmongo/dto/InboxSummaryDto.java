package iuh.dhktpm14.cnm.chatappmongo.dto;

import lombok.Data;

import java.io.Serializable;

@Data
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class InboxSummaryDto implements Serializable {
    private String id;
    private Object room;

}
