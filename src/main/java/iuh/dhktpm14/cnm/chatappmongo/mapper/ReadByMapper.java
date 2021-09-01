package iuh.dhktpm14.cnm.chatappmongo.mapper;

import iuh.dhktpm14.cnm.chatappmongo.dto.ReadByDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.ReadTracking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReadByMapper {

    @Autowired
    private UserMapper userMapper;

    public ReadByDto toReadByDto(ReadTracking readTracking) {
        if (readTracking == null)
            return null;
        var dto = new ReadByDto();
        dto.setReadAt(readTracking.getReadAt());
        dto.setReadByUser(userMapper.toUserProfileDto(readTracking.getUserId()));
        return dto;
    }
}
