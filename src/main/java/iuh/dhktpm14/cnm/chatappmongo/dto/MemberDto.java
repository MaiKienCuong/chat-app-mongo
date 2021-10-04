package iuh.dhktpm14.cnm.chatappmongo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MemberDto implements Serializable, Comparable<MemberDto> {
    private UserProfileDto user;
    private UserProfileDto addByUser;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date addTime;

    @JsonProperty(value = "isAdmin")
    private boolean isAdmin;

    @Override
    public int compareTo(MemberDto o) {
        if (user != null && o != null && o.getUser() != null) {
            var nameOfFirstUser = user.getDisplayName().substring(user.getDisplayName().lastIndexOf(" ") + 1);
            var nameOfSecondUser = o.getUser().getDisplayName().substring(o.getUser().getDisplayName().lastIndexOf(" ") + 1);
            return nameOfFirstUser.compareTo(nameOfSecondUser);
        }
        return 0;
    }
}
