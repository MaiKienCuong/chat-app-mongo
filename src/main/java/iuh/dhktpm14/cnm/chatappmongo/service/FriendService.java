package iuh.dhktpm14.cnm.chatappmongo.service;

import iuh.dhktpm14.cnm.chatappmongo.entity.Friend;
import iuh.dhktpm14.cnm.chatappmongo.repository.FriendRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FriendService {

    @Autowired
    private FriendRepository friendRepository;

    /**
     * lấy danh sách bạn bè của người dùng hiện tại
     */
    public Page<Friend> getAllFriendOfUser(String currentUserId, Pageable pageable) {
        return friendRepository.getAllFriendOfUser(currentUserId, pageable);
    }

    /**
     * kiểm tra xem hai người có phải bạn bè hay không
     */
    public boolean isFriend(String currentUserId, String friendIdToCheck) {
        return friendRepository.isFriend(currentUserId, friendIdToCheck);
    }

    /**
     * xóa bạn bè
     * khi hai người là bạn bè thì trong database có 2 record
     * nên khi một người xóa bạn bè với người kia thì phải xóa cả 2 record
     */
    public void deleteFriend(String currentUserId, String friendIdToDelete) {
        friendRepository.deleteFriend(currentUserId, friendIdToDelete);
    }

    public Friend save(Friend friend) {
        return friendRepository.save(friend);
    }

    public Optional<Friend> findById(String id) {
        return friendRepository.findById(id);
    }

    public List<Friend> findAll() {
        return friendRepository.findAll();
    }

}
