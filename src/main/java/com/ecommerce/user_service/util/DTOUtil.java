package com.ecommerce.user_service.util;

import com.ecommerce.user_service.dto.UserDTO;
import com.ecommerce.user_service.entity.UserEntity;
import com.ecommerce.user_service.exception.UserPictureIOException;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class DTOUtil
{
  public static UserEntity toEntity (UserDTO userDTO)
  {
    if (userDTO == null)
      return null;
    try
    {
      return UserEntity.builder ()
                       .email (userDTO.getEmail ())
                       .role (userDTO.getRole ())
                       .name (userDTO.getName ())
                       .password (userDTO.getPassword ())
                       .picture (userDTO.getPicture () != null ? userDTO.getPicture ().getBytes () : null)
                       .build ();
    }
    catch (IOException ex)
    {
      throw new UserPictureIOException ("Can not convert user picture data!", ex.getCause ());
    }
  }
  
  public static UserDTO toDTO (UserEntity userEntity)
  {
    if (userEntity == null)
      return null;
    return UserDTO.builder ().email (userEntity.getEmail ()).role (userEntity.getRole ()).name (userEntity.getName ()).build ();
  }
  
  public static List <UserDTO> toDTOs (List <UserEntity> userEntities)
  {
    if (userEntities == null)
      return null;
    return userEntities.stream ().map (DTOUtil::toDTO).collect (Collectors.toList ());
  }
}
