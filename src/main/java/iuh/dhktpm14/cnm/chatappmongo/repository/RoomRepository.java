package iuh.dhktpm14.cnm.chatappmongo.repository;

import iuh.dhktpm14.cnm.chatappmongo.entity.Room;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends MongoRepository<Room, String> {

    /**
     * tìm room(type=ONE) chung giữa hai người
     */
    @Aggregation(pipeline = {
            "{$match: {$and: [{'members.userId': ?0}, {'members.userId': ?1}, {'members': {$size: 2}}, {type: 'ONE'}]}}",
            "{$limit: 1}" })
    Room findCommonRoomBetween(String firstUserId, String secondUserId);

    /**
     * tìm danh sách room(type=GROUP) chung giữa 2 người
     */
    @Query("{$and: [{'members.userId': ?0}, {'members.userId': ?1}, {$expr: {$gt: [{$size: '$members'}, 2]}}, {type: 'GROUP'}]}")
    List<Room> findCommonGroupBetween(String firstUserId, String secondUserId);

    /**
     * đếm số nhóm chung giữa hai người
     */
    @Query(value = "{$and: [{'members.userId': ?0}, {'members.userId': ?1}, {$expr: {$gt: [{$size: '$members'}, 2]}}, {type: 'GROUP'}]}", count = true)
    long countCommonGroupBetween(String firstUserId, String secondUserId);

}
