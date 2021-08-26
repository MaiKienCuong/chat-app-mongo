package iuh.dhktpm14.cnm.chatappmongo.payload;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class TokenRefreshRequest {
	
	@NotBlank(message = "refresh token không được rỗng")
	private String refreshToken;
	

}
