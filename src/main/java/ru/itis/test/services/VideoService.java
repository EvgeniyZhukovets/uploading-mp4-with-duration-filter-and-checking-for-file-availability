package ru.itis.test.services;

import org.springframework.web.multipart.MultipartFile;
import ru.itis.test.dto.ResponseDto;

public interface VideoService {

    ResponseDto uploadVideo(MultipartFile multipartFile);
}
