package iuh.dhktpm14.cnm.chatappmongo.repository;

import iuh.dhktpm14.cnm.chatappmongo.entity.UserReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserReportRepository extends MongoRepository<UserReport, String> {
    Page<UserReport> findAllByOrderByCreateAtDesc(Pageable pageable);

    Page<UserReport> findAllBySeenOrderByCreateAtDesc(boolean seen, Pageable pageable);

    Page<UserReport> findAllByFromIdOrderByCreateAtDesc(String fromId, Pageable pageable);

    Page<UserReport> findAllByToIdOrderByCreateAtDesc(String toId, Pageable pageable);

    long countAllByToId(String toId);

}
