package iuh.dhktpm14.cnm.chatappmongo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.FriendStatus;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.OnlineStatus;
import iuh.dhktpm14.cnm.chatappmongo.util.Utils;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileDto implements Serializable, Comparable<UserProfileDto> {
    private String id;
    private String displayName;
    private String imageUrl;

    private OnlineStatus onlineStatus;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date lastOnline;
    private FriendStatus friendStatus;
    private String phoneNumber;
    private boolean blockMe;

    @Override
    public int compareTo(UserProfileDto o) {
        if (displayName == null)
            return 0;
        if (o == null)
            return 0;
        if (o.getDisplayName() == null)
            return 0;

        String fullNameOfFirstUser = Utils.removeAccent(displayName.trim());
        String fullNameOfSecondUser = Utils.removeAccent(o.getDisplayName().trim());

        String nameOfFirstUser;
        String nameOfSecondUser;
        if (fullNameOfFirstUser.contains(" "))
            nameOfFirstUser = fullNameOfFirstUser.substring(fullNameOfFirstUser.lastIndexOf(" ") + 1);
        else
            nameOfFirstUser = fullNameOfFirstUser;
        if (fullNameOfSecondUser.contains(" "))
            nameOfSecondUser = fullNameOfSecondUser.substring(fullNameOfSecondUser.lastIndexOf(" ") + 1);
        else
            nameOfSecondUser = fullNameOfSecondUser;

        return nameOfFirstUser.compareTo(nameOfSecondUser);
    }

}
