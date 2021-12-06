package iuh.dhktpm14.cnm.chatappmongo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MonthDto implements Serializable {
    private Date from;
    private Date to;
}
