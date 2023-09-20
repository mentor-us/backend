package com.hcmus.mentor.backend.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateRoleRequest {
    @NotBlank
    @Size(min = 0, max = 100)
    private String name;
    private String description;
    private List<String> permissions;
}
