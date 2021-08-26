package iuh.dhktpm14.cnm.chatappmongo.repository;

import iuh.dhktpm14.cnm.chatappmongo.entity.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {

    Optional<RefreshToken> findById(String id);

    Optional<RefreshToken> findByToken(String token);

    void deleteByUserId(String userId);

}
