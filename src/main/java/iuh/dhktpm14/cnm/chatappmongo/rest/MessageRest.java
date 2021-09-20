package iuh.dhktpm14.cnm.chatappmongo.rest;

import io.swagger.annotations.ApiOperation;
import iuh.dhktpm14.cnm.chatappmongo.dto.MessageDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.ReactionDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.ReadByDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.InboxMessage;
import iuh.dhktpm14.cnm.chatappmongo.entity.Message;
import iuh.dhktpm14.cnm.chatappmongo.entity.Reaction;
import iuh.dhktpm14.cnm.chatappmongo.entity.ReadTracking;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.MessageNotFoundException;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.UnAuthenticateException;
import iuh.dhktpm14.cnm.chatappmongo.mapper.MessageMapper;
import iuh.dhktpm14.cnm.chatappmongo.mapper.ReactionMapper;
import iuh.dhktpm14.cnm.chatappmongo.mapper.ReadByMapper;
import iuh.dhktpm14.cnm.chatappmongo.payload.MessageResponse;
import iuh.dhktpm14.cnm.chatappmongo.repository.InboxMessageRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.InboxRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.MessageRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.ReadTrackingRepository;
import iuh.dhktpm14.cnm.chatappmongo.service.MessageService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/messages")
@CrossOrigin("${spring.security.cross_origin}")
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
    private ReadByMapper readByMapper;

    @Autowired
    private ReactionMapper reactionMapper;

    @Autowired
    private ReadTrackingRepository readTrackingRepository;

    @Autowired
    private ReadTrackingService readTrackingService;

    @Autowired
    private MessageService messageService;

    /**
     * lấy tất cả tin nhắn của inboxId
     */
    @GetMapping("/inbox/{inboxId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Lấy tất cả tin nhắn của một cuộc trò chuyện")
    public ResponseEntity<?> getAllMessageOfInbox(@PathVariable String inboxId, Pageable pageable, @ApiIgnore @AuthenticationPrincipal User user) {
        if (user == null)
            throw new UnAuthenticateException();
        // kiểm tra xem inboxId có thuộc về user hiện tại hay không
        if (inboxRepository.existsByIdAndOfUserId(inboxId, user.getId())) {
            var inbox = inboxRepository.findByIdAndOfUserId(inboxId, user.getId()).get();
            if (! inbox.isEmpty()) {
                /*
                cập nhật số tin nhắn mới bằng 0, và set tin nhắn đã đọc là tin nhắn mới nhất
                 */
                System.out.println("user.getId() = " + user.getId());
                System.out.println("roomId = " + inbox.getRoomId());
//                readTrackingService.updateReadTracking(user.getId(), inbox.getRoomId());
                /*
                 lấy ra danh sách messageIds của inbox này, phân trang và sắp xếp theo messageCreateAt: -1
                 sau lệnh này nếu k chỉ định size thì mặc định chỉ lấy 20 document
                 tức là số lượng document là đã bị giới hạn
                 */
                Page<InboxMessage> inboxMessages = inboxMessageRepository.getAllInboxMessageOfInbox(inboxId, pageable);
                if (inboxMessages.isEmpty())
                    return ResponseEntity.ok(new PageImpl<>(new ArrayList<>(), pageable, inboxMessages.getTotalElements()));
                List<String> messageIds = inboxMessages.getContent().stream().map(InboxMessage::getMessageId)
                        .collect(Collectors.toList());
                /*
                lấy ra danh sách message trong collection message mà có id nằm trong list messageIds,
                do trước đó đã phân trang và sắp xếp theo messageCreateAt: -1
                nên truy vấn này truyền vào Pageable.unpaged() (không phân trang) để lấy tất cả document khớp,
                truy vấn trước trả về số bản ghi giới hạn không phải là getAll trong collection
                 */
                Page<Message> messagePage = messageRepository.findAllByIdInMessageIdsPaged(messageIds, Pageable.unpaged());
                /*
                xem hàm toMessageDto
                 */
                return ResponseEntity.ok(toMessageDto(messagePage, inboxMessages));
            }
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * lấy tin nhắn theo id
     */
    @GetMapping("/{messageId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Lấy chi tiết tin nhắn theo id")
    public ResponseEntity<?> getById(@PathVariable String messageId, @ApiIgnore @AuthenticationPrincipal User user) {
        if (user == null)
            throw new UnAuthenticateException();
        if (messageService.checkPermissionToSeeMessage(messageId, user.getId())) {
            Optional<Message> messageOptional = messageRepository.findById(messageId);
            if (messageOptional.isEmpty())
                throw new MessageNotFoundException();
            return ResponseEntity.ok(messageOptional.get());
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * xóa tin nhắn, chỉ thay đổi content=Đã xóa và set deleted=true, k xóa trong db
     */
    @DeleteMapping("/{messageId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Gỡ một tin nhắn")
    public ResponseEntity<?> deleteById(@PathVariable String messageId, @ApiIgnore @AuthenticationPrincipal User user) {
        if (user == null)
            throw new UnAuthenticateException();
        Optional<Message> messageOptional = messageRepository.findById(messageId);
        if (messageOptional.isEmpty())
            throw new MessageNotFoundException();
        if (messageService.checkPermissionToSeeMessage(messageId, user.getId())) {
            var message = messageOptional.get();
            // kiểm tra xem người gửi có phải người dùng hiện tại hay không mới cho xóa
            if (user.getId().equals(message.getSenderId())) {
                messageService.deleteMessage(messageId, user.getDisplayName() + " đã xóa nội dung này");
                return ResponseEntity.ok().build();
            }
        }
        return ResponseEntity.badRequest().body(new MessageResponse("Bạn không có quyền xóa tin nhắn này"));
    }

    /**
     * bày tỏ cảm xúc về một tin nhắn
     */
    @PostMapping("/react/{messageId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Bày tỏ cảm xúc về một tin nhắn")
    public ResponseEntity<?> addReactToMessage(@PathVariable String messageId, @RequestBody Reaction reaction,
                                               @ApiIgnore @AuthenticationPrincipal User user) {
        if (user == null)
            throw new UnAuthenticateException();
        reaction.setReactByUserId(user.getId());
        messageService.addReactToMessage(messageId, reaction);
        return ResponseEntity.ok().build();
    }

    /**
     * lấy danh sách những người đã xem tin nhắn
     */
    @GetMapping("/reads/{messageId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Chi tiết tin nhắn: Lấy danh sách những người đã xem tin nhắn này")
    public ResponseEntity<?> getReadbyes(@PathVariable String messageId, @ApiIgnore @AuthenticationPrincipal User user) {
        if (user == null)
            throw new UnAuthenticateException();
        Optional<Message> optionalMessage = messageRepository.findById(messageId);
        if (optionalMessage.isEmpty())
            throw new MessageNotFoundException();
        List<ReadTracking> readTracking = readTrackingRepository.findAllByMessageId(messageId);
        Set<ReadByDto> dto = readTracking.stream().map(readByMapper::toReadByDto)
                .sorted().collect(Collectors.toCollection(LinkedHashSet::new));
        return ResponseEntity.ok(dto);
    }

    /**
     * lấy danh sách người đã bày tỏ cảm xúc về một tin nhắn
     */
    @GetMapping("/react/{messageId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Chi tiết tin nhắn: Lấy danh sách những người đã bày tỏ cảm xúc về tin nhắn này")
    public ResponseEntity<?> getReaction(@PathVariable String messageId, @ApiIgnore @AuthenticationPrincipal User user) {
        if (user == null)
            throw new UnAuthenticateException();
        Optional<Message> optionalMessage = messageRepository.findById(messageId);
        if (optionalMessage.isEmpty())
            throw new MessageNotFoundException();
        List<Reaction> reactions = optionalMessage.get().getReactions();
        if (reactions != null) {
            List<ReactionDto> dto = reactions.stream().map(reactionMapper::toReactionDto)
                    .sorted(Comparator.comparing(x -> x.getReactByUser().getDisplayName()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.ok(new ArrayList<>());
    }

    /**
     * chuyển từ page message qua page messageDto
     */
    private Page<?> toMessageDto(Page<Message> messagePage, Page<InboxMessage> inboxMessagePage) {
        List<Message> content = messagePage.getContent();
        List<MessageDto> dto = content.stream().map(x -> messageMapper.toMessageDto(x.getId()))
                .collect(Collectors.toList());

        /*
        tham số thứ 2 truyền vào là pageAble của truy vấn trước đó trong collection inboxMessage
        tham số thứ 3 truyền vào là totalElement của truy vấn trước đó trong collection inboxMessage,
        vì đã phân trang ở truy vấn thứ nhât, truy vấn thứ 2 chỉ là getAll trong collection Message
        nến không thể lấy các giá trị này từ truy vấn thứ 2
         */
        Collections.reverse(dto);
        return new PageImpl<>(dto, inboxMessagePage.getPageable(), inboxMessagePage.getTotalElements());
    }

}
