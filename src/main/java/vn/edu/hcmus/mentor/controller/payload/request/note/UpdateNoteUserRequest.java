package vn.edu.hcmus.mentor.controller.payload.request.note;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNoteUserRequest {

    private List<String> userIds;
}