package iuh.dhktpm14.cnm.chatappmongo.repository;

import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    List<User> findAllByDisplayNameContainingIgnoreCaseOrderByDisplayNameAsc(String displayName);

    List<User> findByIdIn(List<String> ids);

    Optional<User> findDistinctByUsername(String userName);

    Optional<User> findDistinctByEmail(String email);

    Optional<User> findDistinctByPhoneNumber(String phoneNumber);

    boolean existsByUsername(String userName);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

}
