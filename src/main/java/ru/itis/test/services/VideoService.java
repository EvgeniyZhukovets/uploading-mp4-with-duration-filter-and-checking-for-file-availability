package ru.itis.test.services;

import org.springframework.web.multipart.MultipartFile;

public interface VideoService {

    String uploadVideo(MultipartFile multipartFile);

}
