package com.ecommerce.user_service.service;

import com.ecommerce.user_service.enums.SecurityEventType;
import com.ecommerce.user_service.property.LoggingSensitiveProperties;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j @Service public class LogSanitizerService
{
  private static final int MAX_PARAM_LENGTH = 300;
  private static final String REDACTED_PLACEHOLDER = "*****";
  private static final String UNKNOWN_CLIENT = "Unknown";
  
  private static final Set <String> HARDCODED_EXACT_REDACTED_KEYS = Set.of ("authorization",
                                                                                    "cookie",
                                                                                    "set-cookie",
                                                                                    "x-forwarded-for",
                                                                                    "password",
                                                                                    "secret",
                                                                                    "token");
  
  private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile ("\\b(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14})\\b");
  private static final Pattern JWT_TOKEN_PATTERN = Pattern.compile ("\\beyJ[0-9a-zA-Z_-]{10,}\\.([0-9a-zA-Z_-]+\\.)?[0-9a-zA-Z_-]{10,}\\b");
  private static final Pattern EMAIL_PATTERN = Pattern.compile ("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");
  
  private final Cache <Integer, String> messageCache = CacheBuilder.newBuilder ().maximumSize (1000).expireAfterWrite (5, TimeUnit.MINUTES).build ();
  
  private static final Map <LogLevel, String> LOG_TITLES = Map.of (LogLevel.INFO,
                                                                   "### Info Details ###",
                                                                   LogLevel.WARN,
                                                                   "### Warning Details ###",
                                                                   LogLevel.ERROR,
                                                                   "### Error Details ###");
  
  private static final Set <Pattern> keyPatterns = new HashSet <> ();
  private static final Set <Pattern> valuePatterns = new HashSet <> ();
  
  private final LoggingSensitiveProperties sensitiveProperties;
  
  @Autowired public LogSanitizerService (LoggingSensitiveProperties sensitiveProperties)
  {
    this.sensitiveProperties = sensitiveProperties;
  }
  
  @PostConstruct public void validateSensitiveConfig ()
  {
    if (sensitiveProperties.getExactRedactedKeys () == null)
    {
      sensitiveProperties.setExactRedactedKeys (new ArrayList <> (HARDCODED_EXACT_REDACTED_KEYS));
    }
    else
    {
      // enforce adding hardcoded exact redacted keys to the sensitive properties
      for (String key : HARDCODED_EXACT_REDACTED_KEYS)
      {
        if (!sensitiveProperties.getExactRedactedKeys ().contains (key))
        {
          sensitiveProperties.getExactRedactedKeys ().add (key);
        }
      }
    }
    
    initPatternsFromProperties (sensitiveProperties.getPatternRedactedKeys (), keyPatterns);
    initPatternsFromProperties (sensitiveProperties.getPatternRedactedValues (), valuePatterns);
  }
  
  private void initPatternsFromProperties (List <String> patterns, Set <Pattern> result)
  {
    if (patterns == null)
    {
      return;
    }
    for (String pattern : patterns)
    {
      try
      {
        result.add (Pattern.compile (pattern));
      }
      catch (Exception ex)
      {
        log.warn ("Cannot compile logging sensitive pattern {}", pattern);
      }
    }
  }
  
  public String error (String message)
  {
    return prepareLogContent (LogLevel.ERROR, message, null);
  }
  
  public String error (String message, HttpServletRequest request)
  {
    return prepareLogContent (LogLevel.ERROR, message, request);
  }
  
  public String warning (String message)
  {
    return prepareLogContent (LogLevel.WARN, message, null);
  }
  
  public String warning (String message, HttpServletRequest request)
  {
    return prepareLogContent (LogLevel.WARN, message, request);
  }
  
  public String info (String message)
  {
    return prepareLogContent (LogLevel.INFO, message, null);
  }
  
  public String info (String message, HttpServletRequest request)
  {
    return prepareLogContent (LogLevel.INFO, message, request);
  }
  
  public String securityEvent (SecurityEventType eventType, String message, HttpServletRequest request)
  {
    String newMessage = String.format ("Security event: %s, %s", eventType.name (), message == null ? "not provided" : message);
    return prepareLogContent (LogLevel.WARN, newMessage, request);
  }
  
  public String securityEvent (SecurityEventType eventType, String message)
  {
    String newMessage = String.format ("Security event: %s, %s", eventType.name (), message == null ? "not provided" : message);
    return prepareLogContent (LogLevel.WARN, newMessage, null);
  }
  
  private String prepareLogContent (LogLevel level, String message, HttpServletRequest request)
  {
    
    try
    {
      StringBuilder logBuilder = new StringBuilder ();
      logBuilder.append (LOG_TITLES.getOrDefault (level, "### Log Details ###")).append ("\n");
      
      // Add request details if available
      if (request != null)
      {
        logBuilder.append ("Request    : ").append (getRequestSummary (request)).append ("\n");
        logBuilder.append ("Client     : ").append (getClientInfo (request)).append ("\n");
        logBuilder.append ("Headers    : ").append (getSanitizedHeaders (request)).append ("\n");
        logBuilder.append ("Parameters : ").append (getSanitizedParameters (request)).append ("\n");
      }
      
      // Add log message if available
      if (StringUtils.isNotBlank (message))
      {
        logBuilder.append ("Message    : ").append (secureMessage (message)).append ("\n");
      }
      
      return logBuilder.toString ();
      
    }
    catch (Exception loggingException)
    {
      log.error ("Failed to sanitize log event", loggingException);
      return "Failed to sanitize log event";
    }
  }
  
  private Map <String, String> getSanitizedHeaders (HttpServletRequest request)
  {
    if (request == null)
    {
      return Map.of ();
    }
    return Collections.list (request.getHeaderNames ()).stream ().collect (Collectors.toMap (header -> header, header ->
    {
      String value = request.getHeader (header);
      return isSensitiveHeader (header) ? REDACTED_PLACEHOLDER : redactContent (value);
    }, (v1, v2) -> String.join(";", v1, v2)));
  }
  
  private boolean isSensitiveHeader (String headerName)
  {
    return sensitiveProperties.getExactRedactedKeys ().stream ().anyMatch (headerName::equalsIgnoreCase) ||
           keyPatterns.stream ().anyMatch (pattern -> pattern.matcher (headerName).find ());
  }
  
  private Map <String, String> getSanitizedParameters (HttpServletRequest request)
  {
    if (request == null)
    {
      return Map.of ();
    }
    return request.getParameterMap ().entrySet ().stream ().collect (Collectors.toMap (Map.Entry::getKey, entry ->
    {
      String value = StringUtils.join (entry.getValue (), ",");
      if (isSensitiveParameter (entry.getKey ()))
      {
        return REDACTED_PLACEHOLDER;
      }
      return redactContent (StringUtils.abbreviate (value, MAX_PARAM_LENGTH));
    }));
  }
  
  private boolean isSensitiveParameter (String paramName)
  {
    return sensitiveProperties.getExactRedactedKeys ().stream ().anyMatch (paramName::equalsIgnoreCase) ||
           keyPatterns.stream ().anyMatch (pattern -> pattern.matcher (paramName).find ());
  }
  
  public String redactContent (String value)
  {
    if (value == null)
      return null;
    
    // apply hardcoded redacted patterns
    String redacted = CREDIT_CARD_PATTERN.matcher (value).replaceAll ("****-****-****-****");
    
    redacted = JWT_TOKEN_PATTERN.matcher (redacted).replaceAll ("*****.*****.*****");
    
    redacted = EMAIL_PATTERN.matcher (redacted).replaceAll ("*****@*****");
    
    // apply Configurable redacted patterns
    for (Pattern pattern : valuePatterns)
    {
      redacted = pattern.matcher (redacted).replaceAll (REDACTED_PLACEHOLDER);
    }
    
    return redacted;
  }
  
  private String getClientInfo (HttpServletRequest request)
  {
    return request == null ? "" :
           String.format ("Client: %s [%s]", request.getRemoteAddr (), StringUtils.defaultString (request.getHeader ("User-Agent"), UNKNOWN_CLIENT));
  }
  
  private String getRequestSummary (HttpServletRequest request)
  {
    return request == null ? "Unknown" : String.format ("%s %s", request.getMethod (), StringUtils.defaultIfEmpty (request.getRequestURI (), "Unknown URI"));
  }
  
  private String secureMessage (String message)
  {
    if (message == null)
      return "";
    
    try
    {
      return messageCache.get (Objects.hash (message), // Hash key instead of raw message
                               () ->
                               {
                                 String redacted = redactContent (message);
                                 return sanitizeLogMessage (redacted);
                               });
    }
    catch (ExecutionException e)
    {
      log.warn ("Message processing failed", e);
      return "[REDACTION ERROR]";
    }
  }
  
  private String sanitizeLogMessage (String message)
  {
    if (message == null)
      return "";
    String clean = message.replaceAll ("[\r\n\t]", " ");
    return StringUtils.abbreviate (clean, 1000);
  }
}
