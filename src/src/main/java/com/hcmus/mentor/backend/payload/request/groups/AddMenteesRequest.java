package com.hcmus.mentor.backend.payload.request.groups;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class AddMenteesRequest {
  List<String> emails;
}
