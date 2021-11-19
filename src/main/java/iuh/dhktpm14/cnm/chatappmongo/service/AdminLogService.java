package iuh.dhktpm14.cnm.chatappmongo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import iuh.dhktpm14.cnm.chatappmongo.entity.AdminLog;
import iuh.dhktpm14.cnm.chatappmongo.repository.AdminLogRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AdminLogService {
	
	@Autowired
	private AdminLogRepository logRepository;
	
	public AdminLog writeLog(AdminLog log) {
		return logRepository.save(log);
	}

}
