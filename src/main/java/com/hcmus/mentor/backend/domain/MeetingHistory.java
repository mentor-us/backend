package com.hcmus.mentor.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hcmus.mentor.backend.util.DateUtils;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@Entity
@Builder
@Table(name = "meeting_histories")
@AllArgsConstructor
@JsonIgnoreProperties(value = {"meeting", "modifier"}, allowSetters = true)
public class MeetingHistory implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "time_start")
    private Date timeStart;

    @Column(name = "time_end")
    private Date timeEnd;

    @Column(name = "place")
    private String place;

    @Builder.Default
    @Column(name = "modify_date", nullable = false)
    private Date modifyDate = DateUtils.getDateNowAtUTC() ;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "modifier_id")
    private User modifier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    private Meeting meeting;

    public MeetingHistory() {

    }

    @Override
    public String toString() {
        return "\tLịch hẹn: "
                + "id='"
                + id
                + '\''
                + ", Thời gian bắt đầu="
                + timeStart
                + ", Thời gian kết thúc="
                + timeEnd
                + ", Địa điểm='"
                + place
                + '\''
                + ", Ngày cập nhật ="
                + modifyDate
                + '\n';
    }
}