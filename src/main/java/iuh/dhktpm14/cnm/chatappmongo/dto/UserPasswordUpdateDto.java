package iuh.dhktpm14.cnm.chatappmongo.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserPasswordUpdateDto implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5453472839096222964L;

	
	@NotBlank
	@JsonAlias(value = {"phone", "email"})
	private String username;
	
	private String currentPassword;
	
	private String newPassword;

}
