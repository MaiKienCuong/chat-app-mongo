package iuh.dhktpm14.cnm.chatappmongo.rest;

import io.swagger.annotations.ApiOperation;
import iuh.dhktpm14.cnm.chatappmongo.dto.InboxDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.Inbox;
import iuh.dhktpm14.cnm.chatappmongo.entity.ReadTracking;
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
    private MongoTemplate mongoTemplate;

    /**
     * l???y t???t c??? inbox c???a ng?????i d??ng hi???n t???i
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("L???y danh s??ch cu???c tr?? chuy???n")
    public ResponseEntity<?> getAllInboxOfCurrentUser(@ApiIgnore @AuthenticationPrincipal User user, Pageable pageable) {
        if (user == null)
            throw new UnAuthenticateException();
        Page<Inbox> inboxPage = inboxRepository.getAllInboxOfUser(user.getId(), pageable);
        return ResponseEntity.ok(toInboxDto(inboxPage));
    }

    /**
     * x??a inbox
     */
    @DeleteMapping("/{inboxId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("X??a cu???c tr?? chuy???n")
    public ResponseEntity<?> deleteInbox(@PathVariable String inboxId, @ApiIgnore @AuthenticationPrincipal User user) {
        if (user == null)
            throw new UnAuthenticateException();
        Optional<Inbox> inboxOptional = inboxRepository.findById(inboxId);
        if (inboxOptional.isEmpty())
            return ResponseEntity.badRequest().body(new MessageResponse("X??a kh??ng th??nh c??ng"));
        var inbox = inboxOptional.get();
        if (user.getId().equals(inbox.getOfUserId())) {
            var criteria = Criteria.where("_id").is(inboxId);
            var update = new Update();
            update.set("empty", true);
            // c???p nh???t thu???c t??nh empty=true
            mongoTemplate.updateFirst(Query.query(criteria), update, Inbox.class);
            // x??a t???t c??? message li??n k???t v???i inbox n??y, kh??ng x??a trong collection message
            inboxMessageRepository.deleteAllMessageOfInbox(inbox.getId());
            // reset s??? tin nh???n ch??a ?????c th??nh 0
            resetUnreadMessageToZero(inbox.getRoomId(), user.getId());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * chuy???n t??? Page inbox sang page inboxDto
     */
    private Page<?> toInboxDto(Page<Inbox> inboxPage) {
        List<Inbox> content = inboxPage.getContent();
        List<InboxDto> dto = content.stream()
                .map(x -> inboxMapper.toInboxDto(x))
                .collect(Collectors.toList());
        return new PageImpl<>(dto, inboxPage.getPageable(), inboxPage.getTotalElements());
    }

    private void resetUnreadMessageToZero(String roomId, String userId) {
        var criteria = Criteria.where("roomId").is(roomId)
                .and("userId").is(userId);
        var update = new Update();
        update.set("unReadMessage", 0);
        mongoTemplate.updateFirst(Query.query(criteria), update, ReadTracking.class);
    }

}
