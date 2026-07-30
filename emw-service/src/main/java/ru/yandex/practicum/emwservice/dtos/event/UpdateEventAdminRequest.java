package ru.yandex.practicum.emwservice.dtos.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.emwservice.model.util.StateActionAdmin;


@Getter
@Setter
@ToString(callSuper = true)
public class UpdateEventAdminRequest extends UpdateEventBaseRequest {
    private StateActionAdmin stateAction;
}
