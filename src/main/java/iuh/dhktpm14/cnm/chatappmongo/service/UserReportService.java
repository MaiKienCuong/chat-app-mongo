package iuh.dhktpm14.cnm.chatappmongo.service;

import iuh.dhktpm14.cnm.chatappmongo.entity.UserReport;
import iuh.dhktpm14.cnm.chatappmongo.repository.UserReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserReportService {

    @Autowired
    private UserReportRepository userReportRepository;

    public Optional<UserReport> findById(String id) {
        return userReportRepository.findById(id);
    }

    public UserReport save(UserReport userReport) {
        return userReportRepository.save(userReport);
    }

    public Page<UserReport> findAll(Pageable pageable) {
        return userReportRepository.findAllByOrderByCreateAtDesc(pageable);
    }

    public Page<UserReport> findAll(boolean seen, Pageable pageable) {
        return userReportRepository.findAllBySeenOrderByCreateAtDesc(seen, pageable);
    }

    public Page<UserReport> findAllByFromId(String fromId, Pageable pageable) {
        return userReportRepository.findAllByFromIdOrderByCreateAtDesc(fromId, pageable);
    }

    public Page<UserReport> findAllByToId(String toId, Pageable pageable) {
        return userReportRepository.findAllByToIdOrderByCreateAtDesc(toId, pageable);
    }

    public long countAllByToId(String toId) {
        return userReportRepository.countAllByToId(toId);
    }

}
