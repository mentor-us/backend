package com.hcmus.mentor.backend.controller.payload;

import com.hcmus.mentor.backend.domain.File;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileModel implements Serializable {

    private String id;

    private String filename;

    private long size;

    private String url;

    @Builder.Default
    private UploadStatus uploadStatus = UploadStatus.Success;

    public FileModel(File file) {
        if (file == null) {
            return;
        }

        this.id = file.getId();
        this.filename = file.getFilename();
        this.size = file.getSize();
        this.url = file.getUrl();
        this.uploadStatus = UploadStatus.Success;
    }

    public enum UploadStatus {
        Uploading,
        Success,
        Standby,
        Fail
    }
}