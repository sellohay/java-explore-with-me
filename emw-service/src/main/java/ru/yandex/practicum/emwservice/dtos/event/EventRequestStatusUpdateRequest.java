package ru.yandex.practicum.emwservice.dtos.event;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.emwservice.model.util.RequestUpdateStatus;

import java.util.List;

@Getter
@Setter
@ToString
public class EventRequestStatusUpdateRequest {

    @NotEmpty(message = "Request list can't be empty")
    List<Long> requestIds;

    @NotNull(message = "Status can't be empty")
    RequestUpdateStatus status;
}
