package iuh.dhktpm14.cnm.chatappmongo.dto;

import iuh.dhktpm14.cnm.chatappmongo.enumvalue.FriendStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewProfileDto {
    private UserProfileDto user;
    private FriendStatus friendStatus;
}
