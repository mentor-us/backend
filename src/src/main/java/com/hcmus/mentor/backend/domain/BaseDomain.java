package com.hcmus.mentor.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.cglib.core.Local;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;


@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BaseDomain implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    protected String id;

    /**
     * Created date
     */
    @Column(name = "created_date", nullable = false)
    protected LocalDateTime createdDate  =  LocalDateTime.now(ZoneOffset.UTC);

    /**
     * Updated date
     */
    @Column(name = "updated_date", nullable = false)
    protected LocalDateTime updatedDate =  LocalDateTime.now(ZoneOffset.UTC);
}