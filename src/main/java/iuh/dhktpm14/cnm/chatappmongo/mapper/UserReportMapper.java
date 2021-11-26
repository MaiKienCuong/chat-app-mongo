package iuh.dhktpm14.cnm.chatappmongo.mapper;

import iuh.dhktpm14.cnm.chatappmongo.dto.UserReportDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.UserReport;
import iuh.dhktpm14.cnm.chatappmongo.service.UserReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserReportMapper {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserReportService userReportService;

    @Autowired
    private MessageMapper messageMapper;

    public UserReportDto toUserReportDto(UserReport userReport) {
        if (userReport == null)
            return null;
        var dto = new UserReportDto();
        dto.setId(userReport.getId());
        dto.setFrom(userMapper.toUserDetailDto(userReport.getFromId()));
        dto.setTo(userMapper.toUserDetailDto(userReport.getToId()));
        dto.setCreateAt(userReport.getCreateAt());
        dto.setContent(userReport.getContent());
        dto.setMessage(messageMapper.toMessageDto(userReport.getMessageId()));
        dto.setSeen(userReport.isSeen());
        dto.setMedia(userReport.getMedia());

        return dto;
    }

    public UserReportDto toUserReportDto(String userReportId) {
        if (userReportId == null)
            return null;
        Optional<UserReport> userReport = userReportService.findById(userReportId);
        return toUserReportDto(userReport.orElse(null));
    }

}
