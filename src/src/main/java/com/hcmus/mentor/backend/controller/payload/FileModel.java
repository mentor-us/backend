package com.hcmus.mentor.backend.controller.payload;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
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

    public enum UploadStatus {
        Uploading,
        Success,
        Standby,
        Fail
    }
}
