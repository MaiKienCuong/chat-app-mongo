package iuh.dhktpm14.cnm.chatappmongo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserSummaryDto implements Serializable {
    private String id;
    private String username;
    private String displayName;
    private String imageUrl;
    private String roles;
    private String accessToken;

    public UserSummaryDto(User user, String accessToken) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.displayName = user.getDisplayName();
        this.imageUrl = user.getImageUrl();
        this.roles = user.getRoles();
        this.accessToken = accessToken;
    }
}
