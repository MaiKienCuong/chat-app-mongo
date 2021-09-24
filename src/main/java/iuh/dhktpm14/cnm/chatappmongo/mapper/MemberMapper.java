package iuh.dhktpm14.cnm.chatappmongo.mapper;

import iuh.dhktpm14.cnm.chatappmongo.dto.MemberDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {

    @Autowired
    private UserMapper userMapper;

    public MemberDto toMemberDto(Member member) {
        if (member == null)
            return null;
        var dto = new MemberDto();
        dto.setUser(userMapper.toUserProfileDto(member.getUserId()));
        dto.setAddByUser(userMapper.toUserProfileDto(member.getAddByUserId()));
        dto.setAddTime(member.getAddTime());
        dto.setAdmin(member.isAdmin());
        return dto;
    }

}
