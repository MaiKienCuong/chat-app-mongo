package iuh.dhktpm14.cnm.chatappmongo.repository;

import iuh.dhktpm14.cnm.chatappmongo.entity.Block;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlockRepository extends MongoRepository<Block, String> {

    @Query(value = "{userId: ?0}", sort = "{createAt: -1}")
    Page<Block> findAllByUserId(String userId, Pageable pageable);

    @Query(value = "{userId: ?0, blockId: ?1}", exists = true)
    boolean checkBlock(String currentUserId, String anotherUserId);

    @Query(value = "{userId: ?0, blockId: ?1}", delete = true)
    long unBlock(String currentUserId, String anotherUserId);

    @Aggregation(pipeline = {
            "{$match: {$and: [{userId: ?0}, {blockId: ?1}]}}",
            "{$sort: {createAt: -1}}",
            "{$limit: 1}"
    })
    Optional<Block> findByUserIdAndBlockUserId(String currentUserId, String anotherUserId);

}
