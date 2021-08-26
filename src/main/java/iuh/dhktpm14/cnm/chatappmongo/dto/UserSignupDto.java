package iuh.dhktpm14.cnm.chatappmongo.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Data
public class UserSignupDto implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3627257505114527017L;

	private String id;

	private String displayName;

	@NotBlank(message = "Mật khẩu không được để trống")
	@Length(min = 8, message = "Mật khẩu phải từ 8 ký tự trở lên")
	private String password;

	@NotBlank(message = "số điện thoại không được rỗng")
	@Length(max = 11, min = 10, message =  "số điện thoại phải từ 10-11 số")
	@Pattern(regexp = "[0-9]+", message = "số điện thoại phải là số")
	private String phoneNumber;

}
