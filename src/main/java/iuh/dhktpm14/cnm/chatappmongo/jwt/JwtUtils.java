package iuh.dhktpm14.cnm.chatappmongo.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Getter
@Setter
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);


    @Value("${security.jwt.expiration:86400}")
    private int expiration;

    @Value("${security.jwt.refresh_expiration}")
    private long refreshExpiration;

    @Value("${security.jwt.secret}")
    private String secret;

    public String generateJwtToken(Authentication authentication) {
        var user = (User) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject((user.getUsername()))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + expiration * 1000L))
                .signWith(SignatureAlgorithm.HS512, secret).compact();
    }


    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public Date getExpirationFromJwtToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        }

        return false;
    }


    public String generateTokenFromUsername(String username) {
        return  Jwts.builder().setSubject(username).setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + expiration * 1000L))
                .signWith(SignatureAlgorithm.HS512, secret).compact();
    }

    public String generateJwtRefreshToken(Authentication authentication) {
        var user = (User) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject((user.getUsername()))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + refreshExpiration * 1000L))
                .signWith(SignatureAlgorithm.HS512, secret).compact();
    }

    public void verifyExpiration(String token) {
    }
}
