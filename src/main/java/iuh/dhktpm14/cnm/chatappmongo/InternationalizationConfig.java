package iuh.dhktpm14.cnm.chatappmongo;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Locale;

@Configuration
public class InternationalizationConfig {

    /*
    config file message
    ví dụ ngôn ngữ tiếng Anh sẽ lấy dữ liệu từ file message_en
    ngôn ngữ tiếng Việt sẽ lấy dữ liệu từ file message_vi
     */
    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        var source = new ReloadableResourceBundleMessageSource();
        source.setBasenames("classpath:message");
        source.setDefaultEncoding("UTF-8");
        return source;
    }

    /*
    cấu hình ngôn ngữ mặc định là Việt Nam
     */
    @Bean
    public LocaleResolver localeResolver() {
        var resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(new Locale("vi", "VN"));
        return resolver;
    }

    /*
    config bean validation dùng cho đa ngôn ngữ, message lỗi trả về phụ thuộc vào ngôn ngữ của client
    được gọi khi dùng @Valid
     */
    @Bean
    public LocalValidatorFactoryBean localValidatorFactoryBean(MessageSource messageSource) {
        var bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource);
        return bean;
    }

}
