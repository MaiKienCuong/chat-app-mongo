package iuh.dhktpm14.cnm.chatappmongo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactSync {
    private String name;
    private String phone;
    private UserProfileDto user;
    private boolean isFriend;
}
