package iuh.dhktpm14.cnm.chatappmongo.rest;

import io.swagger.annotations.ApiOperation;
import iuh.dhktpm14.cnm.chatappmongo.dto.FriendRequestReceivedDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.FriendRequestSentDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.Friend;
import iuh.dhktpm14.cnm.chatappmongo.entity.FriendRequest;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.UnAuthenticateException;
import iuh.dhktpm14.cnm.chatappmongo.mapper.FriendMapper;
import iuh.dhktpm14.cnm.chatappmongo.repository.FriendRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.FriendRequestRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.UserRepository;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/friend-request")
@CrossOrigin("${spring.security.cross_origin}")
public class FriendRequestRest {

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendMapper friendMapper;

    /**
     * l???y t???t c??? l???i m???i k???t b???n ???? nh???n ???????c
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("L???y t???t c??? l???i m???i k???t b???n ???? nh???n ???????c")
    public ResponseEntity<?> getAllFriendRequestReceived(@ApiIgnore @AuthenticationPrincipal User user, Pageable pageable) {
        if (user == null)
            throw new UnAuthenticateException();
        Page<FriendRequest> friendRequestPage = friendRequestRepository.getAllFriendRequestReceived(user.getId(), pageable);

        return ResponseEntity.ok(toFriendRequestReceivedDto(friendRequestPage));
    }

    /**
     * l???y t???t c??? l???i m???i k???t b???n ???? g???i ??i
     */
    @GetMapping("/sent")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("L???y t???t c??? l???i m???i k???t b???n ???? g???i ??i")
    public ResponseEntity<?> getAllFriendRequestSent(@ApiIgnore @AuthenticationPrincipal User user, Pageable pageable) {
        if (user == null)
            throw new UnAuthenticateException();
        Page<FriendRequest> friendRequestPage = friendRequestRepository.getAllFriendRequestSent(user.getId(), pageable);

        return ResponseEntity.ok(toFriendRequestSentDto(friendRequestPage));
    }

    /**
     * g???i l???i k???t b???n ?????n m???t ng?????i kh??c
     */
    @PostMapping("/{toId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("G???i l???i m???i k???t b???n ?????n ng?????i kh??c")
    public ResponseEntity<?> addNewFriendRequest(@ApiIgnore @AuthenticationPrincipal User user, @PathVariable String toId) {
        if (user == null)
            throw new UnAuthenticateException();
        // g???i ?????n ch??nh m??nh
        if (toId.equals(user.getId()))
            return ResponseEntity.badRequest().build();
        // g???i ?????n ng?????i kh??ng t???n t???i trong database
        if (! userRepository.existsById(toId))
            return ResponseEntity.badRequest().build();
        // hai ng?????i ???? l?? b???n b??
        if (friendRepository.isFriend(user.getId(), toId))
            return ResponseEntity.badRequest().build();
        // ch??a c?? l???i m???i k???t b???n n??o trong database
        if (! friendRequestRepository.isSent(user.getId(), toId) && ! friendRequestRepository.isReceived(user.getId(), toId)) {
            var friendRequest = FriendRequest.builder()
                    .fromId(user.getId())
                    .toId(toId)
                    .build();
            friendRequestRepository.save(friendRequest);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();

    }

    /**
     * ch???p nh???n l???i m???i k???t b???n
     */
    @PutMapping("/{idToAccept}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Ch???p nh???n l???i m???i k???t b???n")
    public ResponseEntity<?> acceptFriendRequest(@PathVariable String idToAccept, @ApiIgnore @AuthenticationPrincipal User user) {
        if (user == null)
            throw new UnAuthenticateException();
        // hai ng?????i ???? l?? b???n b??
        if (friendRepository.isFriend(user.getId(), idToAccept))
            return ResponseEntity.badRequest().build();
        // ng?????i kh??ng t???n t???i trong database
        if (! userRepository.existsById(idToAccept))
            return ResponseEntity.badRequest().build();
        // c?? l???i m???i t??? ng?????i ???? trong database
        if (friendRequestRepository.isReceived(user.getId(), idToAccept)) {
            friendRequestRepository.deleteFriendRequest(idToAccept, user.getId());
            // l??u 2 record trong database
            friendRepository.save(Friend.builder().userId(user.getId()).friendId(idToAccept).build());
            friendRepository.save(Friend.builder().userId(idToAccept).friendId(user.getId()).build());

            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * h???y l???i m???i k???t b???n
     */
    @DeleteMapping("/{deleteId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Thu h???i l???i l???i m???i k???t b???n ???? g???i")
    public ResponseEntity<?> deleteFriendRequest(@ApiIgnore @AuthenticationPrincipal User user, @PathVariable String deleteId) {
        if (user == null)
            throw new UnAuthenticateException();
        // h???y k???t b???n v???i ch??nh m??nh
        if (deleteId.equals(user.getId()))
            return ResponseEntity.badRequest().build();
        // ng?????i d??ng kh??ng t???n t???i trong database
        if (! userRepository.existsById(deleteId))
            return ResponseEntity.badRequest().build();
        // ch??? x??a khi ???? g???i l???i m???i ?????n ng?????i n??y
        if (friendRequestRepository.isSent(user.getId(), deleteId)) {
            friendRequestRepository.deleteFriendRequest(user.getId(), deleteId);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     *
     */
    private Page<?> toFriendRequestReceivedDto(Page<FriendRequest> friendRequestPage) {
        List<FriendRequest> content = friendRequestPage.getContent();
        List<FriendRequestReceivedDto> dto = content.stream()
                .map(x -> friendMapper.toFriendRequestReceived(x))
                .collect(Collectors.toList());
        return new PageImpl<>(dto, friendRequestPage.getPageable(), friendRequestPage.getTotalElements());
    }

    /**
     *
     */
    private Page<?> toFriendRequestSentDto(Page<FriendRequest> friendRequestPage) {
        List<FriendRequest> content = friendRequestPage.getContent();
        List<FriendRequestSentDto> dto = content.stream()
                .map(x -> friendMapper.toFriendRequestSent(x))
                .collect(Collectors.toList());
        return new PageImpl<>(dto, friendRequestPage.getPageable(), friendRequestPage.getTotalElements());
    }

}
