package com.hcmus.mentor.backend.util;

import com.hcmus.mentor.backend.repository.SystemConfigRepository;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class MailUtils {
  public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
      Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
  private final SystemConfigRepository systemConfigRepository;

  public MailUtils(SystemConfigRepository systemConfigRepository) {
    this.systemConfigRepository = systemConfigRepository;
  }

  public static boolean isValidEmail(String emailStr) {
    Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
    return matcher.matches();
  }

  public boolean isValidDomain(String email) {
    //        Object values = systemConfigRepository.findByKey("valid_domain").getValue();
    //        ArrayList<String> VALID_DOMAINS = (ArrayList<String>) values;
    //
    //        return VALID_DOMAINS.stream()
    //                .anyMatch(email::contains);
    return true;
  }
}
