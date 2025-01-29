package com.ecommerce.user_service.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target ({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention (RetentionPolicy.RUNTIME)
@Constraint (validatedBy = UserEmailValidator.class)
public @interface ValidUserPicture
{
  String message () default "Invalid user picture";
  Class<?>[] groups () default {};
  
  Class<? extends Payload>[] payload () default {};
}
