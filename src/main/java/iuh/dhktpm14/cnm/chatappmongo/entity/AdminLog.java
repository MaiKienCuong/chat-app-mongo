package iuh.dhktpm14.cnm.chatappmongo.entity;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document()
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminLog implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 9046282216797032360L;
	
	@Id
	private String id;
	
	private String content;
	
	private String relatedObjectId;
	
	private String handlerObjectId;
	
	private Date time;
	

}
