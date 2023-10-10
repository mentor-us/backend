package com.hcmus.mentor.backend.payload.request.groups;

import com.hcmus.mentor.backend.entity.Group;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateGroupRequest {
  @NotBlank
  @Size(min = 1, max = 100)
  private String name;

  @Size(max = 200)
  private String description;

  private Group.Status status;
  private Date timeStart;
  private Date timeEnd;
  private String groupCategory;
}
