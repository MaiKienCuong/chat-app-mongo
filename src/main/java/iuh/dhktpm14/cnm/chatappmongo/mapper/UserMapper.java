package iuh.dhktpm14.cnm.chatappmongo.mapper;

import iuh.dhktpm14.cnm.chatappmongo.dto.UserDetailDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.UserProfileDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.ViewProfileDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.FriendStatus;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.MyException;
import iuh.dhktpm14.cnm.chatappmongo.service.AppUserDetailService;
import iuh.dhktpm14.cnm.chatappmongo.service.BlockService;
import iuh.dhktpm14.cnm.chatappmongo.service.FriendRequestService;
import iuh.dhktpm14.cnm.chatappmongo.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserMapper {

    @Autowired
    private AppUserDetailService userDetailService;

    @Autowired
    private FriendRequestService friendRequestService;

    @Autowired
    private FriendService friendService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private BlockService blockService;

    private User authenticate() {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user == null) {
            String message = messageSource.getMessage("unauthorized", null, LocaleContextHolder.getLocale());
            throw new MyException(message);
        }
        return user;
    }

    public UserProfileDto toUserProfileDto(String userId) {
        authenticate();
        if (userId == null)
            return null;
        Optional<User> user = userDetailService.findById(userId);
        return toUserProfileDto(user.orElse(null));
    }

    public UserProfileDto toUserProfileDto(User user) {
        var currentUser = authenticate();
        if (user == null)
            return null;
        var dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setDisplayName(user.getDisplayName());
        dto.setImageUrl(user.getImageUrl());
        dto.setOnlineStatus(user.getOnlineStatus());
        dto.setLastOnline(user.getLastOnline());
        dto.setPhoneNumber(user.getPhoneNumber());

        if (! currentUser.getId().equals(user.getId())) {
            dto.setFriendStatus(getFriendStatus(user, currentUser));
            dto.setBlockMe(blockService.checkThisUserBlockMe(currentUser.getId(), user.getId()));
        }

        return dto;
    }

    private FriendStatus getFriendStatus(User user, User currentUser) {
        var friendStatus = FriendStatus.NONE;
        if (friendService.isFriend(currentUser.getId(), user.getId()))
            friendStatus = FriendStatus.FRIEND;
        else if (friendRequestService.isSent(currentUser.getId(), user.getId()))
            friendStatus = FriendStatus.SENT;
        else if (friendRequestService.isReceived(currentUser.getId(), user.getId()))
            friendStatus = FriendStatus.RECEIVED;
        return friendStatus;
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

    public ViewProfileDto toViewProfileDto(User user) {
        var currentUser = authenticate();
        if (user == null)
            return null;
        var dto = new ViewProfileDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setDisplayName(user.getDisplayName());
        dto.setGender(user.getGender());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setImageUrl(user.getImageUrl());
        dto.setOnlineStatus(user.getOnlineStatus());
        dto.setLastOnline(user.getLastOnline());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setEmail(user.getEmail());

        if (! currentUser.getId().equals(user.getId())) {
            dto.setFriendStatus(getFriendStatus(user, currentUser));
            dto.setBlockMe(blockService.checkThisUserBlockMe(currentUser.getId(), user.getId()));
        }

        return dto;
    }

}
