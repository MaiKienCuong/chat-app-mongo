package iuh.dhktpm14.cnm.chatappmongo.dto;

import lombok.Data;

import java.util.Set;

@Data
public class BlockListDto {
    private String id;
    private UserProfileDto user;
    private Set<UserProfileDto> blockUsers;
}
