package vn.edu.hcmus.mentor.service;

import vn.edu.hcmus.mentor.domain.Meeting;
import vn.edu.hcmus.mentor.domain.Task;
import vn.edu.hcmus.mentor.service.dto.EventDto;

import java.util.Date;
import java.util.List;

public interface EventService {

    List<EventDto> getAllOwnEvents(String userId);

    List<EventDto> mergeEvents(List<Meeting> meetings, List<Task> tasks);

    List<EventDto> getAllEventsByDate(String userId, Date date);

    List<EventDto> getAllEventsByMonth(String userId, Date date);

}
