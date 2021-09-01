package iuh.dhktpm14.cnm.chatappmongo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmailDto implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3123439003816087522L;
	private String email;
}
