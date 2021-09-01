package iuh.dhktpm14.cnm.chatappmongo.rest;

import io.swagger.annotations.ApiOperation;
import iuh.dhktpm14.cnm.chatappmongo.dto.InboxDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.Inbox;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.UnAuthenticateException;
import iuh.dhktpm14.cnm.chatappmongo.mapper.InboxMapper;
import iuh.dhktpm14.cnm.chatappmongo.payload.MessageResponse;
import iuh.dhktpm14.cnm.chatappmongo.repository.InboxMessageRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.InboxRepository;
import iuh.dhktpm14.cnm.chatappmongo.service.InboxService;
import iuh.dhktpm14.cnm.chatappmongo.service.ReadTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inboxs")
@CrossOrigin("${spring.security.cross_origin}")
public class InboxRest {

    @Autowired
    private InboxRepository inboxRepository;

    @Autowired
    private InboxMessageRepository inboxMessageRepository;

    @Autowired
    private InboxMapper inboxMapper;

    @Autowired
    private InboxService inboxService;

    @Autowired
    private ReadTrackingService readTrackingService;

    /**
     * lấy tất cả inbox của người dùng hiện tại
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Lấy danh sách cuộc trò chuyện")
    public ResponseEntity<?> getAllInboxOfCurrentUser(@ApiIgnore @AuthenticationPrincipal User user, Pageable pageable) {
        if (user == null)
            throw new UnAuthenticateException();
        Page<Inbox> inboxPage = inboxRepository.getAllInboxOfUser(user.getId(), pageable);
        return ResponseEntity.ok(toInboxDto(inboxPage));
    }

    /**
     * xóa inbox
     */
    @DeleteMapping("/{inboxId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Xóa cuộc trò chuyện")
    public ResponseEntity<?> deleteInbox(@PathVariable String inboxId, @ApiIgnore @AuthenticationPrincipal User user) {
        if (user == null)
            throw new UnAuthenticateException();
        Optional<Inbox> inboxOptional = inboxRepository.findById(inboxId);
        if (inboxOptional.isEmpty())
            return ResponseEntity.badRequest().body(new MessageResponse("Xóa không thành công. Thử lại sau"));
        var inbox = inboxOptional.get();
        if (user.getId().equals(inbox.getOfUserId())) {
            inboxService.updateEmptyStatusInbox(inboxId, true);

            // xóa tất cả message liên kết với inbox này, không xóa trong collection message
            inboxMessageRepository.deleteAllMessageOfInbox(inboxId);

            // reset số tin nhắn chưa đọc thành 0
            readTrackingService.resetUnreadMessageToZero(inbox.getRoomId(), user.getId());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * chuyển từ Page inbox sang page inboxDto
     */
    private Page<?> toInboxDto(Page<Inbox> inboxPage) {
        List<Inbox> content = inboxPage.getContent();
        List<InboxDto> dto = content.stream()
                .map(x -> inboxMapper.toInboxDto(x))
                .sorted()
                .collect(Collectors.toList());
        return new PageImpl<>(dto, inboxPage.getPageable(), inboxPage.getTotalElements());
    }

}
