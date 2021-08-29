package iuh.dhktpm14.cnm.chatappmongo.repository;

import iuh.dhktpm14.cnm.chatappmongo.entity.Friend;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendRepository extends MongoRepository<Friend, String> {

    /**
     * lấy danh sách bạn bè của một người
     */
    Page<Friend> findAllByUserIdOrderByCreateAtDesc(String currentUserId, Pageable pageable);

    /**
     * kiểm tra xem hai người có phải bạn bè hay không
     */
    boolean existsByUserIdAndFriendId(String currentUserId, String friendIdToCheck);

    /**
     * xóa bạn bè
     */
    void deleteByUserIdAndFriendId(String currentUserId, String friendIdToDelete);

}
