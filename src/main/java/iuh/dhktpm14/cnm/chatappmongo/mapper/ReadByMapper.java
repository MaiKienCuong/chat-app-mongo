package iuh.dhktpm14.cnm.chatappmongo.mapper;

import iuh.dhktpm14.cnm.chatappmongo.dto.ReadByDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.ReadBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReadByMapper {

    @Autowired
    private UserMapper userMapper;

    public ReadByDto toReadByDto(ReadBy readBy) {
        var dto = new ReadByDto();
        dto.setReadAt(readBy.getReadAt());
        dto.setReadByUser(userMapper.toUserProfileDto(readBy.getReadByUserId()));
        return dto;
    }
}
