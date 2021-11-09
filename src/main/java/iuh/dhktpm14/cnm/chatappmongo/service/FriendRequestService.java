package iuh.dhktpm14.cnm.chatappmongo.service;

import iuh.dhktpm14.cnm.chatappmongo.entity.FriendRequest;
import iuh.dhktpm14.cnm.chatappmongo.repository.FriendRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FriendRequestService {

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    /**
     * lấy danh sách lời mời kết bạn đã nhận, truyền vào id của người dùng hiện tại
     */
    public Page<FriendRequest> getAllFriendRequestReceived(String currentUserId, Pageable pageable) {
        return friendRequestRepository.getAllFriendRequestReceived(currentUserId, pageable);
    }

    /**
     * lấy danh sách lời mời kết bạn đã gửi đi, truyền vào id của người dùng hiện tại
     */
    public Page<FriendRequest> getAllFriendRequestSent(String currentUserId, Pageable pageable) {
        return friendRequestRepository.getAllFriendRequestSent(currentUserId, pageable);
    }

    /**
     * kiểm tra xem mình đã gửi lời mời kết bạn đến người này hay chưa
     */
    public boolean isSent(String currentUserId, String toId) {
        return friendRequestRepository.isSent(currentUserId, toId);
    }

    /**
     * kiểm tra xem đã người này đã gửi lời mời kết bạn đến mình hay chưa
     */
    public boolean isReceived(String currentUserId, String fromId) {
        return friendRequestRepository.isReceived(currentUserId, fromId);
    }

    /**
     * xóa lời mời kết bạn
     */
    public void deleteFriendRequest(String fromId, String toId) {
        friendRequestRepository.deleteFriendRequest(fromId, toId);
    }

    public Optional<FriendRequest> findById(String id) {
        return friendRequestRepository.findById(id);
    }

    public FriendRequest save(FriendRequest friendRequest) {
        return friendRequestRepository.save(friendRequest);
    }

    public int countFriendRequestReceived(String currentUserId) {
        return friendRequestRepository.countFriendRequestReceived(currentUserId);
    }

    public int countFriendRequestSent(String currentUserId) {
        return friendRequestRepository.countFriendRequestSent(currentUserId);
    }

    public Optional<FriendRequest> findByFromIdAndToId(String fromId, String toId) {
        return friendRequestRepository.findByFromIdAndToId(fromId, toId);
    }
}
