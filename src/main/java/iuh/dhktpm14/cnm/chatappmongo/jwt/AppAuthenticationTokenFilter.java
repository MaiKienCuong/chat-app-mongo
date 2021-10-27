package iuh.dhktpm14.cnm.chatappmongo.jwt;

import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.MyException;
import iuh.dhktpm14.cnm.chatappmongo.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Slf4j
public class AppAuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageSource messageSource;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.info("in do filter internal");
        String jwt = parseJwt(request);
        if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
            String userId = jwtUtils.getUserIdFromJwtToken(jwt);

            Optional<User> findById = userRepository.findById(userId);
            if (findById.isEmpty()) {
                String userNotFound = messageSource.getMessage("user_not_found", null, RequestContextUtils.getLocale(request));
                log.error(userNotFound);
                throw new MyException(userNotFound);
            }
            var userDetails = findById.get();
            var authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                    userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            var context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
        }
//		filterChain la 1 chuoi cac filter lien tiep nhau
        filterChain.doFilter(request, response);
//		sau khi xac thuc xong, doFilter(request, response) dung de chuyen quyen dieu khien cho filter tiep theo trong chuoi filter
    }

    private String parseJwt(HttpServletRequest request) {
        /*
         * Cookie[] cookies = request.getCookies(); String accessToken = null; if
         * (cookies != null) { Optional<Cookie> cookie = Arrays.stream(cookies)
         * .filter(c -> c.getName().equals("access_token")) .findFirst(); if
         * (cookie.isPresent()) accessToken = cookie.get().getValue(); }
         *
         * return accessToken;
         */

        String headerAuth = request.getHeader("Authorization");
        log.info("header auth = {}", headerAuth);
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer")) {
            headerAuth = headerAuth.replace("Bearer", "").trim();
            return headerAuth;
        }
        log.error("Authorization header is null, path = {}", request.getRequestURI());
        return null;
    }
}
