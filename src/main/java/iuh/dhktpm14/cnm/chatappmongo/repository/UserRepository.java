package iuh.dhktpm14.cnm.chatappmongo.repository;

import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    /**
     * tìm kiếm user theo tên gần đúng, k phân biệt hoa thường
     */
    List<User> findAllByDisplayNameContainingIgnoreCaseOrderByDisplayNameAsc(String displayName);

    List<User> findByIdIn(List<String> ids);

    Optional<User> findDistinctByUsername(String userName);

    Optional<User> findDistinctByEmail(String email);

    @Query("{$or: [{username: ?0}, {email: ?0}, {phoneNumber: ?0}]}")
    Optional<User> findDistinctByPhoneNumberOrUsernameOrEmail(String phoneNumber);

    boolean existsByUsername(String userName);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

}
