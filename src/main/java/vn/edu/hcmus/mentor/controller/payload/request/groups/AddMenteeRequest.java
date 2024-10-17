package vn.edu.hcmus.mentor.controller.payload.request.groups;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddMenteeRequest {

    private String email;
}
