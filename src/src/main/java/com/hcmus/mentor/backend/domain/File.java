package com.hcmus.mentor.backend.domain;

import com.hcmus.mentor.backend.controller.payload.FileModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@Entity
@AllArgsConstructor
@Table(name = "files")
public class File implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String filename;

    private long size;

    private String url;

    public File() {

    }

    public File(FileModel fileModel) {
        this.filename = fileModel.getFilename();
        this.size = fileModel.getSize();
        this.url = fileModel.getUrl();
    }
}
