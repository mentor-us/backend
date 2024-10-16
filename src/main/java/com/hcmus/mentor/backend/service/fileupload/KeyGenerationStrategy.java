package com.hcmus.mentor.backend.service.fileupload;

/**
 * Interface for key generation strategy.
 */
public interface KeyGenerationStrategy {
    /**
     * Generate blob key.
     *
     * @param mimeType Blob MIME type.
     * @return Blob key.
     */
    String generateBlobKey(String mimeType);
}
