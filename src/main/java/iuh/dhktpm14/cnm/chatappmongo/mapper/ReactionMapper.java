package iuh.dhktpm14.cnm.chatappmongo.mapper;

import iuh.dhktpm14.cnm.chatappmongo.dto.ReactionDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.Reaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReactionMapper {

    @Autowired
    private UserMapper userMapper;

    public ReactionDto toReadByDto(Reaction reaction) {
        var dto = new ReactionDto();
        dto.setType(reaction.getType());
        dto.setReactByUser(userMapper.toUserProfileDto(reaction.getReactByUserId()));
        return dto;
    }
}
