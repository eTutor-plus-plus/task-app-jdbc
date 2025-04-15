package at.jku.dke.task_app.jdbc.validation;

import at.jku.dke.task_app.jdbc.dto.ModifyJDBCTaskGroupDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Custom validator for fields in {@link ModifyJDBCTaskGroupDto}.
 */
public class ValidTaskGroupNumberValidator implements ConstraintValidator<ValidTaskGroupNumber, ModifyJDBCTaskGroupDto> {

    @Override
    public boolean isValid(ModifyJDBCTaskGroupDto value, ConstraintValidatorContext context) {
        if (value == null)
            return false;

        return isNotEmpty(value.createStatements())
            && isNotEmpty(value.insertStatementsDiagnose())
            && isNotEmpty(value.insertStatementsSubmission());
    }

    private boolean isNotEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
