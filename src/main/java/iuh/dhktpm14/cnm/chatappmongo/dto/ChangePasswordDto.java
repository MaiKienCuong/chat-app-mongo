package iuh.dhktpm14.cnm.chatappmongo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import iuh.dhktpm14.cnm.chatappmongo.validation.FieldNotMatch;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldNotMatch(first = "oldPass", second = "newPass", message = "{oldPass_newPass_must_be_not_match}")
public class ChangePasswordDto {

    @NotBlank(message = "{oldPass_not_empty}")
    private String oldPass;

    @NotBlank(message = "{newPass_not_empty}")
    private String newPass;

}
