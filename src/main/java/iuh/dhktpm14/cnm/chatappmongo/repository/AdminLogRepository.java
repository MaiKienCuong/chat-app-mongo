package iuh.dhktpm14.cnm.chatappmongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import iuh.dhktpm14.cnm.chatappmongo.entity.AdminLog;

@Repository
public interface AdminLogRepository extends MongoRepository<AdminLog, String>{

}
