package iuh.dhktpm14.cnm.chatappmongo.rest;

import io.swagger.annotations.ApiOperation;
import iuh.dhktpm14.cnm.chatappmongo.dto.MessageDto;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
    private MongoTemplate mongoTemplate;

    @Autowired
    private ReadTrackingRepository readTrackingRepository;

    /**
     * l???y t???t c??? tin nh???n c???a inboxId
     */
    @GetMapping("/inbox/{inboxId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("L???y t???t c??? tin nh???n c???a m???t cu???c tr?? chuy???n")
    public ResponseEntity<?> getAllMessageOfInbox(@PathVariable String inboxId, Pageable pageable, @ApiIgnore @AuthenticationPrincipal User user) {
        if (user == null)
            throw new UnAuthenticateException();
        // ki???m tra xem inboxId c?? thu???c v??? user hi???n t???i hay kh??ng
        if (inboxRepository.existsByIdAndOfUserId(inboxId, user.getId())) {
            var inbox = inboxRepository.findByIdAndOfUserId(inboxId, user.getId());
            if (! inbox.isEmpty()) {
                /*
                c???p nh???t s??? tin nh???n m???i b???ng 0, v?? set tin nh???n ???? ?????c l?? tin nh???n m???i nh???t
                 */
                updateReadTracking(user.getId(), inbox.getRoomId());
                /*
                 l???y ra danh s??ch messageIds c???a inbox n??y, ph??n trang v?? s???p x???p theo messageCreateAt: -1
                 sau l???nh n??y n???u k ch??? ?????nh size th?? m???c ?????nh ch??? l???y 20 document
                 t???c l?? s??? l?????ng document l?? ???? b??? gi???i h???n
                 */
                Page<InboxMessage> inboxMessages = inboxMessageRepository.getAllInboxMessageOfInbox(inboxId, pageable);
                if (inboxMessages.isEmpty())
                    return ResponseEntity.ok(new PageImpl<>(new ArrayList<>(), pageable, inboxMessages.getTotalElements()));
                List<String> messageIds = inboxMessages.getContent().stream().map(InboxMessage::getMessageId)
                        .collect(Collectors.toList());
                /*
                l???y ra danh s??ch message trong collection message m?? c?? id n???m trong list messageIds,
                do tr?????c ???? ???? ph??n trang v?? s???p x???p theo messageCreateAt: -1
                n??n truy v???n n??y truy???n v??o Pageable.unpaged() (kh??ng ph??n trang) ????? l???y t???t c??? document kh???p,
                truy v???n tr?????c tr??? v??? s??? b???n ghi gi???i h???n kh??ng ph???i l?? getAll trong collection
                 */
                Page<Message> messagePage = messageRepository.findAllByIdInMessageIdsPaged(messageIds, Pageable.unpaged());
                /*
                xem h??m toMessageDto
                 */
                return ResponseEntity.ok(toMessageDto(messagePage, inboxMessages));
            }
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * l???y danh s??ch tin nh???n ch??a ?????c c???a userId trong roomId
     * v?? c???p nh???t th??nh ???? ?????c
     */
    private void updateReadTracking(String userId, String roomId) {
        var readTracking = readTrackingRepository.findByRoomIdAndUserId(roomId, userId);
        if (readTracking != null && readTracking.getUnReadMessage() != 0) {
            var lastMessage = messageRepository.getLastMessageOfRoom(roomId);
            var criteria = Criteria.where("roomId").is(roomId).and("userId").is(userId);
            var update = new Update();
            update.set("messageId", lastMessage.getId());
            update.set("readAt", new Date());
            update.set("unReadMessage", 0);
            mongoTemplate.updateFirst(Query.query(criteria), update, ReadTracking.class);
        }
    }

    /**
     * l???y tin nh???n theo id
     */
    @GetMapping("/{messageId}/inbox/{inboxId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("L???y chi ti???t tin nh???n theo id")
    public ResponseEntity<?> getById(@PathVariable String messageId, @PathVariable String inboxId, @ApiIgnore @AuthenticationPrincipal User user) {
        // ki???m tra xem tin nh???n n??y c?? trong messageIds c???a inbox c???a user hay kh??ng
        // n???u tin nh???n c?? trong collection message nh??ng kh??ng c?? trong messageIds c???a inbox c???a user th?? kh??ng ???????c xem
        if (user == null)
            throw new UnAuthenticateException();
        var inbox = inboxRepository.findByIdAndOfUserId(inboxId, user.getId());
        if (inbox == null)
            return ResponseEntity.badRequest().build();
        if (inboxMessageRepository.existsByInboxIdAndMessageId(inbox.getId(), messageId)) {
            Optional<Message> messageOptional = messageRepository.findById(messageId);
            if (messageOptional.isEmpty())
                throw new MessageNotFoundException();
            return ResponseEntity.ok(messageOptional.get());
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * x??a tin nh???n, ch??? thay ?????i content=???? x??a v?? set deleted=true, k x??a trong db
     */
    @DeleteMapping("/{messageId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("G??? m???t tin nh???n")
    public ResponseEntity<?> deleteById(@PathVariable String messageId, @ApiIgnore @AuthenticationPrincipal User user) {
        if (user == null)
            throw new UnAuthenticateException();
        Optional<Message> messageOptional = messageRepository.findById(messageId);
        if (messageOptional.isEmpty())
            throw new MessageNotFoundException();
        var message = messageOptional.get();
        // ki???m tra xem ng?????i g???i c?? ph???i ng?????i d??ng hi???n t???i hay kh??ng m???i cho x??a
        if (user.getId().equals(message.getSenderId())) {
            var criteria = Criteria.where("_id").is(messageId);
            var update = new Update();
            update.set("content", "???? x??a");
            update.set("deleted", true);
            mongoTemplate.updateFirst(Query.query(criteria), update, Message.class);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body(new MessageResponse("B???n kh??ng c?? quy???n x??a tin nh???n n??y"));
    }

    /**
     * b??y t??? c???m x??c v??? m???t tin nh???n
     */
    @PostMapping("/react/{messageId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("B??y t??? c???m x??c v??? m???t tin nh???n")
    public ResponseEntity<?> addReactToMessage(@PathVariable String messageId, @RequestBody Reaction reaction,
                                               @ApiIgnore @AuthenticationPrincipal User user) {
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
     * l???y danh s??ch nh???ng ng?????i ???? xem tin nh???n
     */
    @GetMapping("/readby/{messageId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Chi ti???t tin nh???n: L???y danh s??ch nh???ng ng?????i ???? xem tin nh???n n??y")
    public ResponseEntity<?> getReadbyes(@PathVariable String messageId, @ApiIgnore @AuthenticationPrincipal User user) {
        if (user == null)
            throw new UnAuthenticateException();
        Optional<Message> optionalMessage = messageRepository.findById(messageId);
        if (optionalMessage.isEmpty())
            return ResponseEntity.badRequest().build();
        List<ReadTracking> readTracking = readTrackingRepository.findAllByMessageId(messageId);
        Set<ReadByDto> dto = readTracking.stream().map(readByMapper::toReadByDto)
                .sorted(Comparator.comparing(ReadByDto::getReadAt))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return ResponseEntity.ok(dto);
    }

    /**
     * l???y danh s??ch ng?????i ???? b??y t??? c???m x??c v??? m???t tin nh???n
     */
    @GetMapping("/react/{messageId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Chi ti???t tin nh???n: L???y danh s??ch nh???ng ng?????i ???? b??y t??? c???m x??c v??? tin nh???n n??y")
    public ResponseEntity<?> getReaction(@PathVariable String messageId, @ApiIgnore @AuthenticationPrincipal User user) {
        if (user == null)
            throw new UnAuthenticateException();
        Optional<Message> optionalMessage = messageRepository.findById(messageId);
        if (optionalMessage.isEmpty())
            return ResponseEntity.badRequest().build();
        List<Reaction> reactions = optionalMessage.get().getReactions();
        return ResponseEntity.ok(reactions.stream().map(x -> reactionMapper.toReactionDto(x)).collect(Collectors.toList()));
    }

    /**
     * chuy???n t??? page message qua page messageDto
     */
    private Page<?> toMessageDto(Page<Message> messagePage, Page<InboxMessage> inboxMessagePage) {
        List<Message> content = messagePage.getContent();
        List<MessageDto> dto = content.stream().map(x -> messageMapper.toMessageDto(x.getId())).collect(Collectors.toList());

        /*
        tham s??? th??? 2 truy???n v??o l?? pageAble c???a truy v???n tr?????c ???? trong collection inboxMessage
        tham s??? th??? 3 truy???n v??o l?? totalElement c???a truy v???n tr?????c ???? trong collection inboxMessage,
        v?? ???? ph??n trang ??? truy v???n th??? nh??t, truy v???n th??? 2 ch??? l?? getAll trong collection Message
        n???n kh??ng th??? l???y c??c gi?? tr??? n??y t??? truy v???n th??? 2
         */
        Collections.reverse(dto);
        return new PageImpl<>(dto, inboxMessagePage.getPageable(), inboxMessagePage.getTotalElements());
    }

}
