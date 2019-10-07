package ru.itis.test.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface VideoService {

    File uploadVideo(MultipartFile multipartFile);

}
