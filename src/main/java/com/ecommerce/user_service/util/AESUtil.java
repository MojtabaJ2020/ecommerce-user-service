package com.ecommerce.user_service.util;

import com.ecommerce.user_service.property.EncryptionProperties;
import com.ecommerce.user_service.exception.EncryptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component public class AESUtil
{
  private final EncryptionProperties encryptionProperties;
  private Cipher cipher;
  private final byte[] FIXED_IV = new byte[] {42, -12, 85, -66, 23, 9, -78, 63, -18, 77, 101, -34, 4, -7, 89, 12};
  private final IvParameterSpec IV_SPEC = new IvParameterSpec (FIXED_IV);
  Logger logger = LoggerFactory.getLogger (AESUtil.class);
  @Autowired
  public AESUtil (EncryptionProperties encryptionProperties)
  {
    this.encryptionProperties = encryptionProperties;
    init ();
  }
  
  private void init ()
  {
    try
    {
      SecretKey secretKey = new SecretKeySpec (encryptionProperties.getAesKey ().getBytes (StandardCharsets.UTF_8), "AES");
      this.cipher = Cipher.getInstance ("AES/CBC/PKCS5Padding");
      cipher.init (Cipher.ENCRYPT_MODE, secretKey, IV_SPEC);
    }
    catch (Exception ex)
    {
      logger.error ("Cannot initialize AESUtil {}", ex.getMessage ());
      throw new EncryptionException ("Cannot initialize AESUtil!", ex);
    }
  }
  
  public String encrypt (String data)
  {
    try
    {
      byte[] encryptedData = cipher.doFinal (data.getBytes (StandardCharsets.UTF_8));
      return Base64.getEncoder ().encodeToString (encryptedData);
    }
    catch (Exception ex)
    {
      logger.error ("Cannot encrypt data {}", ex.getMessage ());
      throw new EncryptionException ("A problem occurred when decrypting data!", ex);
    }
  }
  
  public String decrypt (String encryptedData)
  {
    try
    {
      byte[] originalData = cipher.doFinal (Base64.getDecoder ().decode (encryptedData));
      return new String (originalData, StandardCharsets.UTF_8);
    }
    catch (Exception ex)
    {
      logger.error ("Cannot decrypt data {}", ex.getMessage ());
      throw new EncryptionException ("A problem occurred when decrypting data!", ex);
    }
  }
}