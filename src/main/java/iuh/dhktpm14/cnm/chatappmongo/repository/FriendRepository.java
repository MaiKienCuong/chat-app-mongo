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
    Page<Friend> findAllByUserIdOrderByCreateAtDesc(String userId, Pageable pageable);

    /**
     * kiểm tra xem hai người có phải bạn bè hay không
     */
    boolean existsByUserIdAndFriendId(String userId, String friendIdToCheck);

    /**
     * xóa bạn bè
     */
    void deleteByUserIdAndFriendId(String userId, String friendIdToDelete);

}
