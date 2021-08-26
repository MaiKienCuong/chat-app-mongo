package iuh.dhktpm14.cnm.chatappmongo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PhoneNumberDto implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1051015893624156463L;
	private String phoneNumber;
}
