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
        if (user == null)
            return 0;
        if (o == null)
            return 0;
        if (o.getUser() == null)
            return 0;
        return user.compareTo(o.getUser());
    }
}
