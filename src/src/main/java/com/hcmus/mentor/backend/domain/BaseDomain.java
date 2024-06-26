package com.hcmus.mentor.backend.domain;

import com.hcmus.mentor.backend.util.DateUtils;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
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
    protected Date createdDate = DateUtils.getDateNowAtUTC();

    /**
     * Updated date
     */
    @Column(name = "updated_date", nullable = false)
    protected Date updatedDate = DateUtils.getDateNowAtUTC();
}