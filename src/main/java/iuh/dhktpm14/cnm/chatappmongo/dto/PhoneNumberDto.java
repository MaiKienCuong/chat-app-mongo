package iuh.dhktpm14.cnm.chatappmongo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PhoneNumberDto implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1051015893624156463L;
	private String phoneNumber;
}
