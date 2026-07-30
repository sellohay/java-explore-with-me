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
    private Long requesterId;
    @JsonProperty("event")
    private Long eventId;
    private String created;
    private String status;
}
