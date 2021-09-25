package iuh.dhktpm14.cnm.chatappmongo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;
import java.util.Date;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserUpdateDto {

    @Email(message = "{email_invalid}")
    private String email;

    @NotBlank(message = "{displayName_not_empty}")
    private String displayName;

    @NotBlank(message = "{gender_not_empty}")
    private String gender;

    @Past(message = "{dateOfBirth_invalid}")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Ho_Chi_Minh")
    private Date dateOfBirth;

}
