package com.hcmus.mentor.backend.payload.request;

import com.hcmus.mentor.backend.entity.GroupCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateGroupCategoryRequest {
  @NotBlank
  @Size(min = 1, max = 100)
  private String name;

  @Size(max = 200)
  private String description;

  private String iconUrl;
  private List<GroupCategory.Permission> permissions;
}
