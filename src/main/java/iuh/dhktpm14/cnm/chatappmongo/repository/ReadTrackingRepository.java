package iuh.dhktpm14.cnm.chatappmongo.repository;

import iuh.dhktpm14.cnm.chatappmongo.entity.ReadTracking;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReadTrackingRepository extends MongoRepository<ReadTracking, String> {

    /**
     * lấy readTracking theo roomId và userId
     * mục đích là để lấy số tin nhắn chưa đọc trong field unReadMessage
     */
    ReadTracking findByRoomIdAndUserId(String roomId, String userId);

    /**
     * lấy danh sách readTracking theo messageId
     * mục đích là để lấy những id của những người đã đọc tin nhắn này
     */
    List<ReadTracking> findAllByMessageId(String messageId);

    long deleteAllByRoomId(String roomId);

}
