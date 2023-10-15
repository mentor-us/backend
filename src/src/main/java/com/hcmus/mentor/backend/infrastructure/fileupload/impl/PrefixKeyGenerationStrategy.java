package com.hcmus.mentor.backend.infrastructure.fileupload.impl;

import com.hcmus.mentor.backend.infrastructure.fileupload.KeyGenerationStrategy;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PrefixKeyGenerationStrategy implements KeyGenerationStrategy {
  /**
   * generateBlobKey
   *
   * @param MimeType MimeType
   * @return String
   */
  @Override
  public String generateBlobKey(String MimeType) {
    String key = UUID.randomUUID().toString();
    return String.format("%s.%s", key, MimeType);
  }
}
