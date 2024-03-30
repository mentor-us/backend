package com.hcmus.mentor.backend.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@Entity
@Builder
@Table(name = "meeting_histories")
public class MeetingHistory implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id ;

    @Column(name = "time_start")
    private Date timeStart;

    @Column(name = "time_end")
    private Date timeEnd;

    @Column(name = "place")
    private String place;

    @Builder.Default
    @Column(name = "modify_date", nullable = false)
    private Date modifyDate = new Date();

    @ManyToOne
    @JoinColumn(name = "modifier_id")
    private User modifier;

    @ManyToOne
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
