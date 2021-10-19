package iuh.dhktpm14.cnm.chatappmongo.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.dhktpm14.cnm.chatappmongo.payload.MessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AppAccessDeniedHandler implements AccessDeniedHandler {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MessageSource messageSource;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        String message = messageSource.getMessage("access_denied", null, RequestContextUtils.getLocale(request));
        response.getWriter().println(mapper.writeValueAsString(new MessageResponse(message)));

    }

}