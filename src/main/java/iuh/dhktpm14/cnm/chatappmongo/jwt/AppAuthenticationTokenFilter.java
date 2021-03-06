package iuh.dhktpm14.cnm.chatappmongo.jwt;

import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.UserNotFoundException;
import iuh.dhktpm14.cnm.chatappmongo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class AppAuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String jwt = parseJwt(request);
        if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
            String userId = jwtUtils.getUserIdFromJwtToken(jwt);

            Optional<User> findById = userRepository.findById(userId);
            if (findById.isEmpty())
                throw new UserNotFoundException();
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
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer")) {
            headerAuth = headerAuth.replace("Bearer", "").trim();
            return headerAuth;
        }
        return null;
    }
}
