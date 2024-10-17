package vn.edu.hcmus.mentor.controller.payload.request.groups;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class AddMembersRequest {
    List<String> emails;
}
