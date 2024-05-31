package com.hcmus.mentor.backend.service.fileupload;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * Blob storage service.
 */
public interface BlobStorage {

    /**
     * Retrieves an InputStream of a file from blob storage.
     *
     * @param key The key (identifier) of the file to be retrieved.
     * @return InputStream of the requested file.
     */
    InputStream get(String key);

    /**
     * Removes a file from the blob storage.
     *
     * @param key The key (identifier) of the file to be removed.
     */
    void remove(String key);

    /**
     * Uploads a file to the blob storage.
     *
     * @param file The MultipartFile to be uploaded.
     * @param key  The key (identifier) that will be assigned to the uploaded file.
     */
    void post(MultipartFile file, String key);

    void post(byte[] file, String key);

    /**
     * Generates a unique blob key for a file based on its MIME type.
     *
     * @param mimeType The MIME type of the file.
     * @return A unique blob key for the file.
     */
    String generateBlobKey(String mimeType);

    /**
     * Checks if a file exists in the blob storage.
     *
     * @param key The key (identifier) of the file.
     * @return Boolean value indicating whether the file exists or not.
     */
    Boolean exists(String key);

    /**
     * Retrieves the URL of a file stored in blob storage.
     *
     * @param key The key (identifier) of the file.
     * @return A string representing the URL of the file.
     */
    String getUrl(String key);

    String copyFile(String sourceKey);
}
