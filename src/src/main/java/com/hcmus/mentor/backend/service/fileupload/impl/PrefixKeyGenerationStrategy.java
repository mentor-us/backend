package com.hcmus.mentor.backend.service.fileupload.impl;

import com.hcmus.mentor.backend.service.fileupload.KeyGenerationStrategy;

import java.util.UUID;

import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.springframework.stereotype.Service;

/**
 * {@inheritDoc}
 */
@Service
public class PrefixKeyGenerationStrategy implements KeyGenerationStrategy {

    /**
     * {@inheritDoc}
     */
    @Override
    public String generateBlobKey(String mimeType) {
        String extension;
        try {
            extension = MimeTypes.getDefaultMimeTypes().forName(mimeType).getExtension();
        } catch (MimeTypeException e) {
            extension = ".bin";
        }

        String fileName = UUID.randomUUID().toString().replace("-", "");
        return String.format("%s/%s%s", fileName.substring(0, 2), fileName.substring(2), extension);
    }
}
