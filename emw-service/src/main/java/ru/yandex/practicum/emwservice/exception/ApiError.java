package ru.yandex.practicum.emwservice.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ApiError {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> errors;

    private String message;
    private String reason;
    private String status;
    private String timestamp;

}
