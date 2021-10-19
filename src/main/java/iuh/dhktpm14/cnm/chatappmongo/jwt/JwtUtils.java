package iuh.dhktpm14.cnm.chatappmongo.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Getter
@Setter
@Slf4j
public class JwtUtils {

    @Value("${security.jwt.expiration:86400}")
    private int expiration;

    @Value("${security.jwt.refresh_expiration}")
    private long refreshExpiration;

    @Value("${security.jwt.secret}")
    private String secret;

    public String generateJwtAccessTokenFromAuthentication(Authentication authentication) {
        var user = (User) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject((user.getId()))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + expiration * 1000L))
                .signWith(SignatureAlgorithm.HS512, secret).compact();
    }

    public String generateJwtAccessTokenFromUserId(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + expiration * 1000L))
                .signWith(SignatureAlgorithm.HS512, secret).compact();
    }

    public String generateJwtRefreshTokenFromUserId(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + refreshExpiration * 1000L))
                .signWith(SignatureAlgorithm.HS512, secret).compact();
    }

    public String getUserIdFromJwtToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        }

        return false;
    }

}
