package iuh.dhktpm14.cnm.chatappmongo.service;

import iuh.dhktpm14.cnm.chatappmongo.entity.Friend;
import iuh.dhktpm14.cnm.chatappmongo.projection.Count;
import iuh.dhktpm14.cnm.chatappmongo.repository.FriendRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class FriendService {

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    /*
    tìm kiếm bạn bè qua sdt, username, phone
     */
    public Page<Friend> findByUsernameOrPhoneOrDisplayNameRegex(String currentUserId, String regex, Pageable pageable) {
        int count = countByUsernameOrPhoneOrDisplayNameRegex(currentUserId, regex);
        if (count == 0)
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        var aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("userId").is(currentUserId)),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "createAt")),
                Aggregation.lookup("user", "friendId", "_id", "friend"),

                Aggregation.unwind("friend"),

                Aggregation.match(new Criteria().orOperator(
                        Criteria.where("friend.username").regex(".*" + regex + ".*", "i"),
                        Criteria.where("friend.phoneNumber").is(regex),
                        Criteria.where("friend.displayName").regex(".*" + regex + ".*", "i"))),

                Aggregation.project("_id", "userId", "friendId", "createAt"),

                Aggregation.sort(Sort.by(Sort.Direction.DESC, "createAt")),
                Aggregation.skip(((long) pageable.getPageNumber() * pageable.getPageSize())),
                Aggregation.limit(pageable.getPageSize())
        );

        AggregationResults<Friend> results = mongoTemplate.aggregate(aggregation, "friend", Friend.class);
        return new PageImpl<>(results.getMappedResults(), pageable, count);
    }

    /*
    lấy số lượng tìm được qua sdt, username, phone để phân trang
     */
    private int countByUsernameOrPhoneOrDisplayNameRegex(String currentUserId, String regex) {
        var aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("userId").is(currentUserId)),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "createAt")),
                Aggregation.lookup("user", "friendId", "_id", "friend"),

                Aggregation.unwind("friend"),

                Aggregation.match(new Criteria().orOperator(
                        Criteria.where("friend.username").regex(".*" + regex + ".*", "i"),
                        Criteria.where("friend.phoneNumber").is(regex),
                        Criteria.where("friend.displayName").regex(".*" + regex + ".*", "i"))),

                Aggregation.group().count().as("count")
        );

        AggregationResults<Count> results = mongoTemplate.aggregate(aggregation, "friend", Count.class);
        List<Count> mappedResults = results.getMappedResults();
        if (mappedResults.isEmpty())
            return 0;
        if (mappedResults.get(0) == null)
            return 0;
        return mappedResults.get(0).getCount();
    }

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
