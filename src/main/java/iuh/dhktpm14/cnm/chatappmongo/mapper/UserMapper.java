package iuh.dhktpm14.cnm.chatappmongo.mapper;

import iuh.dhktpm14.cnm.chatappmongo.dto.UserDetailDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.UserProfileDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.service.AppUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserMapper {

    @Autowired
    private AppUserDetailService userDetailService;

    public UserProfileDto toUserProfileDto(String userId) {
        if (userId == null)
            return null;
        Optional<User> user = userDetailService.findById(userId);
        if (user.isEmpty()) return null;

        return toUserProfileDto(user.get());
    }

    public UserProfileDto toUserProfileDto(User user) {
        if (user == null)
            return null;
        var dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setDisplayName(user.getDisplayName());
        dto.setImageUrl(user.getImageUrl());
        dto.setOnlineStatus(user.getOnlineStatus());
        dto.setLastOnline(user.getLastOnline());
        return dto;
    }

    public UserDetailDto toUserDetailDto(User user) {
        if (user == null)
            return null;
        var dto = new UserDetailDto();
        dto.setId(user.getId());
        dto.setDisplayName(user.getDisplayName());
        dto.setImageUrl(user.getImageUrl());
        dto.setBlock(user.isBlock());
        dto.setCreateAt(user.getCreateAt());
        dto.setEmail(user.getEmail());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setGender(user.getGender());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setRoles(user.getRoles());
        dto.setUsername(user.getUsername());
        dto.setOnlineStatus(user.getOnlineStatus());
        dto.setLastOnline(user.getLastOnline());
        return dto;
    }

}
