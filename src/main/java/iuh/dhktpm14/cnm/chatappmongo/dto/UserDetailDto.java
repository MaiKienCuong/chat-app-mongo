package iuh.dhktpm14.cnm.chatappmongo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import lombok.Data;

import java.util.Date;

@Data
//@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class UserDetailDto {
    private String id;
    private String username;
    private String displayName;
    private String gender;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date dateOfBirth;

    private String phoneNumber;
    private String email;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date createAt;
    private boolean active;
    private boolean block;
    private boolean enable;
    private String imageUrl;
    private String roles;
    private String accessToken;

    public UserDetailDto(User user, String accessToken) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.displayName = user.getDisplayName();
        this.gender = user.getGender();
        this.dateOfBirth = user.getDateOfBirth();
        this.phoneNumber = user.getPhoneNumber();
        this.active = user.isActive();
        this.block = user.isBlock();
        this.enable = user.isEnable();
        this.imageUrl = user.getImageUrl();
        this.roles = user.getRoles();
        this.email = user.getEmail();
        this.createAt = user.getCreateAt();
        this.accessToken = accessToken;
    }
}
