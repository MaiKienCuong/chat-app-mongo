package iuh.dhktpm14.cnm.chatappmongo.mapper;

import iuh.dhktpm14.cnm.chatappmongo.dto.BlockDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.Block;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.MyException;
import iuh.dhktpm14.cnm.chatappmongo.service.BlockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class BlockMapper {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BlockService blockService;

    @Autowired
    private MessageSource messageSource;

    public BlockDto toBlockDto(Block block) {
        var currentUser = authenticate();
        if (block == null)
            return null;
        if (block.getId() == null)
            return null;
        var dto = new BlockDto();
        dto.setId(block.getId());
//        dto.setUser(userMapper.toUserProfileDto(currentUser));
        dto.setBlockUser(userMapper.toUserProfileDto(block.getBlockId()));
        dto.setCreateAt(block.getCreateAt());
        return dto;
    }

    public BlockDto toBlockDto(String id) {
        authenticate();
        if (id == null)
            return null;
        return toBlockDto(blockService.findById(id).orElse(null));
    }

    private User authenticate() {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user == null) {
            String message = messageSource.getMessage("unauthorized", null, LocaleContextHolder.getLocale());
            throw new MyException(message);
        }
        return user;
    }

}
