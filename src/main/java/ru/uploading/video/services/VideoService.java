package ru.uploading.video.services;

import org.springframework.web.multipart.MultipartFile;
import ru.uploading.video.dto.ResponseDto;

public interface VideoService {

    ResponseDto uploadVideo(MultipartFile multipartFile);
}
