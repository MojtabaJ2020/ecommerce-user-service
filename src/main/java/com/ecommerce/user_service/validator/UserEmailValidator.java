package com.ecommerce.user_service.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UserEmailValidator implements ConstraintValidator <ValidEmail, String>
{
  /**
   * Initializes the validator in preparation for
   * {@link #isValid(Object, ConstraintValidatorContext)} calls.
   * The constraint annotation for a given constraint declaration
   * is passed.
   * <p>
   * This method is guaranteed to be called before any use of this instance for
   * validation.
   * <p>
   * The default implementation is a no-op.
   *
   * @param constraintAnnotation annotation instance for a given constraint declaration
   */
  @Override public void initialize (ValidEmail constraintAnnotation)
  {
    ConstraintValidator.super.initialize (constraintAnnotation);
  }
  
  /**
   * Implements the validation logic.
   * The state of {@code value} must not be altered.
   * <p>
   * This method can be accessed concurrently, thread-safety must be ensured
   * by the implementation.
   *
   * @param value   object to validate
   * @param context context in which the constraint is evaluated
   * @return {@code false} if {@code value} does not pass the constraint
   */
  @Override public boolean isValid (String value, ConstraintValidatorContext context)
  {
    return org.apache.commons.validator.routines.EmailValidator.getInstance ().isValid (value);
    
  }
}
