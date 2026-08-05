package ru.yandex.practicum.emwservice.dtos.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.emwservice.dtos.request.ParticipationRequestDto;

import java.util.List;

@Getter
@Setter
@ToString
public class EventRequestStatusUpdateResult {

    List<ParticipationRequestDto> confirmedRequests;
    List<ParticipationRequestDto> rejectedRequests;
}
