package ru.yandex.practicum.emwservice.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ParticipationRequestDto {

    private Long id;
    @JsonProperty("requester")
    private Long requester_id;
    @JsonProperty("event")
    private Long event_id;
    private String created;
    private String status;
}
