package iuh.dhktpm14.cnm.chatappmongo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordDto implements Serializable {
    @NotBlank(message = "{user_id_not_empty}")
    private String userId;

    @NotBlank(message = "{newPass_not_empty}")
    private String newPassword;
}
