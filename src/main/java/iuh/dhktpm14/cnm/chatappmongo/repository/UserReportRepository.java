package iuh.dhktpm14.cnm.chatappmongo.repository;

import iuh.dhktpm14.cnm.chatappmongo.entity.UserReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserReportRepository extends MongoRepository<UserReport, String> {
    @Query(value = "{}", sort = "{createAt: -1}")
    Page<UserReport> findAllByOrderByCreateAtDesc(Pageable pageable);

    @Query(value = "{seen: ?0}", sort = "{createAt: -1}")
    Page<UserReport> findAllBySeenOrderByCreateAtDesc(boolean seen, Pageable pageable);

    @Query(value = "{fromId: ?0}", sort = "{createAt: -1}")
    Page<UserReport> findAllByFromIdOrderByCreateAtDesc(String fromId, Pageable pageable);

    @Query(value = "{toId: ?0}", sort = "{createAt: -1}")
    Page<UserReport> findAllByToIdOrderByCreateAtDesc(String toId, Pageable pageable);

    long countAllByToId(String toId);

}
