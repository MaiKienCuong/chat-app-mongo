package iuh.dhktpm14.cnm.chatappmongo.mapper;

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
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) return null;
        var dto = new UserProfileDto();
        dto.setId(user.get().getId());
        dto.setDisplayName(user.get().getDisplayName());
        dto.setImageUrl(user.get().getImageUrl());
        return dto;
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

}
