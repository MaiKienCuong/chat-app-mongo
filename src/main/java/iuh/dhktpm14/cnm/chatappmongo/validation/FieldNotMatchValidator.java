package iuh.dhktpm14.cnm.chatappmongo.validation;

import org.springframework.beans.BeanWrapperImpl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class FieldNotMatchValidator implements ConstraintValidator<FieldNotMatch, Object> {

    private String message;
    private String firstFieldName;
    private String secondFieldName;

    @Override
    public void initialize(final FieldNotMatch constraintAnnotation) {
        message = constraintAnnotation.message();
        firstFieldName = constraintAnnotation.first();
        secondFieldName = constraintAnnotation.second();
    }

    @Override
    public boolean isValid(final Object value, final ConstraintValidatorContext context) {

        boolean valid = true;

        final Object firstObject = new BeanWrapperImpl(value).getPropertyValue(firstFieldName);
        final Object secondObject = new BeanWrapperImpl(value).getPropertyValue(secondFieldName);

        valid = ((firstObject == null) && (secondObject == null))
                || ((firstObject != null) && (! firstObject.equals(secondObject)));

        if (! valid) {
            context.buildConstraintViolationWithTemplate(message)
                    .addPropertyNode(secondFieldName)
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
        }

        return valid;
    }
}
