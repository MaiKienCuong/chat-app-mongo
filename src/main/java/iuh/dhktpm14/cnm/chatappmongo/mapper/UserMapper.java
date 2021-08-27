package iuh.dhktpm14.cnm.chatappmongo.mapper;

import iuh.dhktpm14.cnm.chatappmongo.dto.UserDetailDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.UserProfileDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserMapper {

    @Autowired
    private UserRepository userRepository;

    public UserProfileDto toUserProfileDto(String userId) {
        if (userId == null)
            return null;
        Optional<User> user = userRepository.findById(userId);
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
        return dto;
    }

    public UserDetailDto toUserDetailDto(User user) {
        if (user == null)
            return null;
        var dto = new UserDetailDto();
        dto.setId(user.getId());
        dto.setDisplayName(user.getDisplayName());
        dto.setImageUrl(user.getImageUrl());
        dto.setActive(user.isActive());
        dto.setBlock(user.isBlock());
        dto.setCreateAt(user.getCreateAt());
        dto.setEmail(user.getEmail());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setGender(user.getGender());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setRoles(user.getRoles());
        dto.setUsername(user.getUsername());
        return dto;
    }

}
