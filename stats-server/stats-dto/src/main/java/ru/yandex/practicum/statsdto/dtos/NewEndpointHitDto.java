package ru.yandex.practicum.statsdto.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class NewEndpointHitDto {
    @NotBlank
    private String app;
    @NotBlank
    private String uri;
    @NotBlank
    @Pattern(regexp = "^(([0-9]|[1-9][0-9]|1[0-9]{2}" +
            "|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]" +
            "|1[0-9]{2}|2[0-4][0-9]|25[0-5])$")
    private String ip;

    @NotBlank
    private String timestamp;
}
