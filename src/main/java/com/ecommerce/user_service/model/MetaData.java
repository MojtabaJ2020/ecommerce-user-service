package com.ecommerce.user_service.model;

import jakarta.validation.Valid;
import lombok.Builder;

@Valid
@Builder
public class MetaData
{
  String link;
  Pagination pagination;
}
