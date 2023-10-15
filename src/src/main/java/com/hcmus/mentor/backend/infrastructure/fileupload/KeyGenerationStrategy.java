package com.hcmus.mentor.backend.infrastructure.fileupload;

public interface KeyGenerationStrategy {
  String generateBlobKey(String MimeType);
}
