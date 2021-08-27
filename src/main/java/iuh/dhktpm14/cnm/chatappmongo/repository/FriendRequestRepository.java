package iuh.dhktpm14.cnm.chatappmongo.repository;

import iuh.dhktpm14.cnm.chatappmongo.entity.FriendRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendRequestRepository extends MongoRepository<FriendRequest, String> {

    /**
     * lấy danh sách lời mời kết bạn, truyền vào id của người dùng hiện tại
     */
    Page<FriendRequest> findAllByToIdOrderByCreateAtDesc(String currentUserId, Pageable pageable);

    /**
     * kiểm tra xem đã gửi lời mời kết bạn hay chưa
     */
    boolean existsByFromIdAndToId(String fromId, String toId);

    /**
     * xóa lời mời kết bạn
     */
    void deleteByFromIdAndToId(String fromId, String toId);

}
