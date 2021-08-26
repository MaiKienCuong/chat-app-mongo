package iuh.dhktpm14.cnm.chatappmongo.rest;

import iuh.dhktpm14.cnm.chatappmongo.dto.InboxDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.Inbox;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.UnAuthenticateException;
import iuh.dhktpm14.cnm.chatappmongo.mapper.InboxMapper;
import iuh.dhktpm14.cnm.chatappmongo.payload.MessageResponse;
import iuh.dhktpm14.cnm.chatappmongo.repository.InboxMessageRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.InboxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inboxs")
public class InboxRest {

    @Autowired
    private InboxRepository inboxRepository;

    @Autowired
    private InboxMessageRepository inboxMessageRepository;

    @Autowired
    private InboxMapper inboxMapper;

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * lấy tất cả inbox của người dùng hiện tại
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getRoomIdsByUserId(@AuthenticationPrincipal User user, Pageable pageable) {
        if (user == null)
            throw new UnAuthenticateException();
        Page<Inbox> inboxPage = inboxRepository.findAllByOfUserId(user.getId(), pageable);
        return ResponseEntity.ok(toInboxDto(inboxPage));
    }

    /**
     * xóa inbox
     */
    @DeleteMapping("/{inboxId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteInbox(@PathVariable String inboxId, @AuthenticationPrincipal User user) {
        if (user == null)
            throw new UnAuthenticateException();
        Optional<Inbox> inboxOptional = inboxRepository.findById(inboxId);
        if (inboxOptional.isEmpty())
            return ResponseEntity.badRequest().body(new MessageResponse("Xóa không thành công"));
        var inbox = inboxOptional.get();
        if (user.getId().equals(inbox.getOfUserId())) {
            var criteria = Criteria.where("_id").is(inboxId);
            var update = new Update();
            update.set("empty", true);
            mongoTemplate.updateFirst(Query.query(criteria), update, Inbox.class);
            inboxMessageRepository.deleteByInboxId(inbox.getId());
            return ResponseEntity.ok(inbox);
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * chuyển từ Page inbox sang page inboxDto
     */
    private Page<?> toInboxDto(Page<Inbox> inboxPage) {
        List<Inbox> content = inboxPage.getContent();
        List<InboxDto> dto = content.stream().map(x -> inboxMapper.toInboxDto(x)).collect(Collectors.toList());
        return new PageImpl<>(dto, inboxPage.getPageable(), inboxPage.getTotalElements());
    }

}
