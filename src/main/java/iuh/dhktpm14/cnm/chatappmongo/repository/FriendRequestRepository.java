package iuh.dhktpm14.cnm.chatappmongo.repository;

import iuh.dhktpm14.cnm.chatappmongo.entity.FriendRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendRequestRepository extends MongoRepository<FriendRequest, String> {

    /**
     * lấy danh sách lời mời kết bạn đã nhận, truyền vào id của người dùng hiện tại
     */
    @Query(value = "{toId: ?0}", sort = "{createAt: -1}")
    Page<FriendRequest> getAllFriendRequestReceived(String currentUserId, Pageable pageable);

    /**
     * lấy danh sách lời mời kết bạn đã gửi đi, truyền vào id của người dùng hiện tại
     */
    @Query(value = "{fromId: ?0}", sort = "{createAt: -1}")
    Page<FriendRequest> getAllFriendRequestSent(String currentUserId, Pageable pageable);

    /**
     * kiểm tra xem mình đã gửi lời mời kết bạn đến người này hay chưa
     */
    @Query(value = "{fromId: ?0, toId: ?1}", exists = true)
    boolean isSent(String currentUserId, String toId);

    /**
     * kiểm tra xem đã người này đã gửi lời mời kết bạn đến mình hay chưa
     */
    @Query(value = "{toId: ?0, fromId: ?1}", exists = true)
    boolean isReceived(String currentUserId, String toId);

    /**
     * xóa lời mời kết bạn
     */
    @Query(value = "{fromId: ?0, toId: ?1}", delete = true)
    void deleteFriendRequest(String fromId, String toId);

}
