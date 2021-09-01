package iuh.dhktpm14.cnm.chatappmongo.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
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
