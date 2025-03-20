package at.jku.dke.task_app.jdbc.validation;

import at.jku.dke.task_app.jdbc.dto.ModifyJDBCTaskGroupDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Custom validator for numbers in {@link ModifyJDBCTaskGroupDto}.
 */
public class ValidTaskGroupNumberValidator implements ConstraintValidator<ValidTaskGroupNumber, ModifyJDBCTaskGroupDto> {
    /**
     * Creates a new instance of class Valid task group number validator.
     */
    public ValidTaskGroupNumberValidator() {
    }

    @Override
    public boolean isValid(ModifyJDBCTaskGroupDto value, ConstraintValidatorContext context) {
        return value.minNumber() < value.maxNumber();
    }
}
