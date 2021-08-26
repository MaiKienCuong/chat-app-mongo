package iuh.dhktpm14.cnm.chatappmongo.rest;

import iuh.dhktpm14.cnm.chatappmongo.dto.MessageCreateDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.MessageDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.Inbox;
import iuh.dhktpm14.cnm.chatappmongo.entity.InboxMessage;
import iuh.dhktpm14.cnm.chatappmongo.entity.Message;
import iuh.dhktpm14.cnm.chatappmongo.entity.Reaction;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.MessageStatus;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.MessageNotFoundException;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.UnAuthenticateException;
import iuh.dhktpm14.cnm.chatappmongo.mapper.MessageMapper;
import iuh.dhktpm14.cnm.chatappmongo.payload.MessageResponse;
import iuh.dhktpm14.cnm.chatappmongo.repository.InboxMessageRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.InboxRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.MessageRepository;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/messages")
public class MessageRest {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private InboxRepository inboxRepository;

    @Autowired
    private InboxMessageRepository inboxMessageRepository;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * lấy tất cả tin nhắn của inboxId
     */
    @GetMapping("/inbox/{inboxId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> findAllByInboxId(@PathVariable String inboxId, Pageable pageable, @AuthenticationPrincipal User user) {
        if (user == null)
            throw new UnAuthenticateException();
        if (inboxRepository.existsByIdAndOfUserId(inboxId, user.getId())) {
            Inbox inbox = inboxRepository.findByIdAndOfUserId(inboxId, user.getId());
            if (! inbox.isEmpty()) {
                List<InboxMessage> inboxMessages = inboxMessageRepository.findAllByInboxId(inboxId, pageable);
                if (inboxMessages.isEmpty())
                    return ResponseEntity.ok(new ArrayList<>());
                Page<Message> messagePage = messageRepository
                        .findAllByIdInMessageIdsPaged(inboxMessages.stream().map(InboxMessage::getMessageId)
                                .collect(Collectors.toList()), pageable);
                return ResponseEntity.ok(toMessageDto(messagePage));
            }
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * lấy tin nhắn theo id
     */
    @GetMapping("/{messageId}/inbox/{inboxId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> findById(@PathVariable String messageId, @PathVariable String inboxId, @AuthenticationPrincipal User user) {
        // kiểm tra xem tin nhắn này có trong messageIds của inbox của user hay không
        // nếu tin nhắn có trong collection message nhưng không có trong messageIds của inbox của user thì không được xem
        if (user == null)
            throw new UnAuthenticateException();
        Inbox inbox = inboxRepository.findByIdAndOfUserId(inboxId, user.getId());
        if (inbox == null)
            return ResponseEntity.badRequest().build();
        if (inboxMessageRepository.existsByInboxIdAndMessageId(inbox.getId(), messageId)) {
            Optional<Message> messageOptional = messageRepository.findById(messageId);
            if (messageOptional.isEmpty())
                throw new MessageNotFoundException();
            return ResponseEntity.ok(messageOptional.get());
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * xóa tin nhắn, chỉ thay đổi content=Đã xóa và set deleted=true, k xóa trong db
     */
    @DeleteMapping("/{messageId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> delete(@PathVariable String messageId, @AuthenticationPrincipal User user) {
        if (user == null)
            throw new UnAuthenticateException();
        Optional<Message> messageOptional = messageRepository.findById(messageId);
        if (messageOptional.isEmpty())
            throw new MessageNotFoundException();
        var message = messageOptional.get();
        // kiểm tra xem người gửi có phải người dùng hiện tại hay không mới cho xóa
        if (user.getId().equals(message.getSenderId())) {
            message.setContent("Đã xóa");
            message.setDeleted(true);
            return ResponseEntity.ok(messageRepository.save(message));
        }
        return ResponseEntity.badRequest().body(new MessageResponse("Bạn không có quyền xóa tin nhắn này"));
    }

    /**
     * gửi một tin nhắn vào room
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> post(@RequestBody MessageCreateDto messageDto, @AuthenticationPrincipal User user) {
        if (user == null)
            throw new UnAuthenticateException();
        var message = Message.builder()
                .type(messageDto.getType())
                .roomId(messageDto.getRoomId())
                .content(messageDto.getContent())
                .status(MessageStatus.SENT)
                .senderId(user.getId())
                .build();
        return ResponseEntity.ok(messageRepository.save(message));
    }

    /**
     * bày tỏ cảm xúc về một tin nhắn
     */
    @PostMapping("/react/{messageId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addReact(@PathVariable String messageId, @RequestBody Reaction
            reaction, @AuthenticationPrincipal User user) {
        if (user == null)
            throw new UnAuthenticateException();
        reaction.setReactByUserId(user.getId());
        var criteria = Criteria.where("_id").is(messageId);
        var update = new Update();
        update.push("reactions", reaction);
        mongoTemplate.updateFirst(Query.query(criteria), update, Message.class);
        return ResponseEntity.ok().build();
    }

    /**
     * chuyển từ page message qua page messageDto
     */
    private Page<?> toMessageDto(Page<Message> messagePage) {
        List<Message> content = messagePage.getContent();
        List<MessageDto> dto = content.stream().map(x -> messageMapper.toMessageDto(x.getId())).collect(Collectors.toList());
        return new PageImpl<>(dto, messagePage.getPageable(), messagePage.getTotalElements());
    }

}
