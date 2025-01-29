package com.ecommerce.user_service.validator;

public interface Validator <T>
{
  boolean validate(final T object);
}
