package iuh.dhktpm14.cnm.chatappmongo.service;

import iuh.dhktpm14.cnm.chatappmongo.entity.RefreshToken;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.MyException;
import iuh.dhktpm14.cnm.chatappmongo.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class RefreshTokenService {
	
	@Value("${security.jwt.refresh_expiration}")
	private Long refreshTokenDuration;
	
	@Autowired
	private RefreshTokenRepository refreshTokenRepository;
	
	public Optional<RefreshToken> findByToken(String token){
		return refreshTokenRepository.findByToken(token);
	}
	
	public RefreshToken createRefreshToken(String userId, String token) {
		RefreshToken refreshToken = new RefreshToken();
		
		refreshToken.setUserId(userId);
		refreshToken.setExpiredTime(Instant.now().plusMillis(refreshTokenDuration));
		refreshToken.setToken(token);
		
		refreshTokenRepository.save(refreshToken);
		return refreshToken;
	}
	
	public RefreshToken verifyExpiration(RefreshToken token) throws MyException {
		if(token.getExpiredTime().compareTo(Instant.now())<0) {
			refreshTokenRepository.delete(token);
			throw new MyException("refresh_token đã hết hạn. Vui lòng thực hiện một yêu cầu đăng nhập mới");
		}
		return token;
	}
	
	public void saveToken(RefreshToken refreshToken) {
		System.out.println("SAVE : " + refreshToken.getToken());
		refreshTokenRepository.save(refreshToken);
	}
	
	
	public void deleteByUserId(String userId) {
		refreshTokenRepository.deleteByUserId(userId);
	}
	

}
