package com.ecommerce.user_service.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

public class UserPicValidator implements ConstraintValidator<ValidUserPicture,MultipartFile>
{
  private static final List <String> ALLOWED_FILE_TYPES = Arrays.asList ("image/jpeg", "image/png");
  private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB in bytes
  
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
  @Override public void initialize (ValidUserPicture constraintAnnotation)
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
   * @param file   object to validate
   * @param context context in which the constraint is evaluated
   * @return {@code false} if {@code value} does not pass the constraint
   */
  @Override public boolean isValid (MultipartFile file, ConstraintValidatorContext context)
  {
    if (file == null || file.isEmpty()) {
      return true;
    }
    
    if (!ALLOWED_FILE_TYPES.contains (file.getContentType ()))
    {
      return false;
    }
    
    return file.getSize () <= MAX_FILE_SIZE;
  }
}
