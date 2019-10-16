package ru.itis.test.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
public class ResponseDto {

    private String status;
    private HttpStatus httpStatus;
}
