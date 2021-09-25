package iuh.dhktpm14.cnm.chatappmongo.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * annotation đánh dấu hai field phải khác nhau
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FieldNotMatchValidator.class)
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
public @interface FieldNotMatch {

    String first();

    String second();

    String message() default "";

    /*
    hai phuong thuc groups va payload la bat buoc phai co
     */
    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
