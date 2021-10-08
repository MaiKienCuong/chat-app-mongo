package iuh.dhktpm14.cnm.chatappmongo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserSignupDto implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 3627257505114527017L;

    private String id;
    private String displayName;

    @NotBlank(message = "{password_not_empty}")
    @Length(min = 8, message = "{password_must_be_8_character_or_more}")
    private String password;

    @NotBlank(message = "{phone_not_empty}")
    @Length(max = 11, min = 10, message = "{phone_range_invalid}")
    @Pattern(regexp = "[0-9]+", message = "{phone_must_be_number}")
    private String phoneNumber;

}
