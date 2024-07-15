package com.hcmus.mentor.backend.domain;

import com.hcmus.mentor.backend.util.DateUtils;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@MappedSuperclass
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
    @Builder.Default
    protected Date createdDate = DateUtils.getDateNowAtUTC();

    /**
     * Updated date
     */
    @Column(name = "updated_date", nullable = false)
    @Builder.Default
    protected Date updatedDate = DateUtils.getDateNowAtUTC();
}