package ru.yandex.practicum.emwservice.dtos.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.emwservice.model.util.RequestUpdateStatus;

import java.util.List;

@Getter
@Setter
@ToString
public class EventRequestStatusUpdateRequest {

    List<Long> requestIds;

    RequestUpdateStatus status;
}
