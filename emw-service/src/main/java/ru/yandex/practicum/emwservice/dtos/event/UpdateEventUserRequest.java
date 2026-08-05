package ru.yandex.practicum.emwservice.dtos.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.emwservice.model.util.StateAction;


@Getter
@Setter
@ToString(callSuper = true)
public class UpdateEventUserRequest extends UpdateEventBaseRequest {
    private StateAction stateAction;
}