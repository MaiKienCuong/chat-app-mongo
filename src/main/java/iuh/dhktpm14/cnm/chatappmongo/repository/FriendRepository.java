package iuh.dhktpm14.cnm.chatappmongo.repository;

import iuh.dhktpm14.cnm.chatappmongo.entity.Friend;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRepository extends MongoRepository<Friend, String> {

    @Aggregation(pipeline = {
            "{$match: {userId: ?0}}",
            "{$lookup: {from: 'user', localField: 'friendId', foreignField: '_id',as: 'users'}}",
            "{$match: {$or : [{'users.displayName': { $regex: ?1, $options: 'i' }}, {'users.phoneNumber' : ?2} ]}}",
            "{$project: {users:0}}"
    })
    List<Friend> findAllByDisplayNameOrPhoneLike(String currentUserId, String name, String phone);

    /**
     * lấy danh sách bạn bè của người dùng hiện tại
     */
    @Query(value = "{userId: ?0}", sort = "{createAt: -1}")
    Page<Friend> getAllFriendOfUser(String currentUserId, Pageable pageable);

    /**
     * kiểm tra xem hai người có phải bạn bè hay không
     */
    @Query(value = "{$or: [{userId: ?0, friendId: ?1}, {friendId: ?0, userId: ?1}] }", exists = true)
    boolean isFriend(String currentUserId, String friendIdToCheck);

    /**
     * xóa bạn bè
     * khi hai người là bạn bè thì trong database có 2 record
     * nên khi một người xóa bạn bè với người kia thì phải xóa cả 2 record
     */
    @Query(value = "{$or: [{userId: ?0, friendId: ?1}, {friendId: ?0, userId: ?1}] }", delete = true)
    void deleteFriend(String currentUserId, String friendIdToDelete);

}
