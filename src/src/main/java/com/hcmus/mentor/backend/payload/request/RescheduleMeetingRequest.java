package com.hcmus.mentor.backend.payload.request;

import com.hcmus.mentor.backend.entity.Meeting;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RescheduleMeetingRequest {

    @NotNull
    private Date timeStart;

    @NotNull
    private Date timeEnd;

    private String place;
}
