package vn.edu.hcmus.mentor.controller.payload.request.FileStorage;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShareFileRequest {

    @Parameter(required = true)
    private String key;
}
