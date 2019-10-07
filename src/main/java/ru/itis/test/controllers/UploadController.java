package ru.itis.test.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import ru.itis.test.services.VideoService;

import java.io.File;


@RestController
public class UploadController {

    @Autowired
    private VideoService videoService;

    @PostMapping("/uploadVideo")
    public ResponseEntity<?> uploadVideo(
            @RequestParam("video") final MultipartFile multipartFile) {
        return errorHandle(videoService.uploadVideo(multipartFile));
    }

    private ResponseEntity errorHandle(final File file) {
        if (file == null) {
            return new ResponseEntity<>("Ошибка загрузки", new HttpHeaders(), HttpStatus.OK);
        }
        return new ResponseEntity<>("Success", new HttpHeaders(), HttpStatus.OK);
    }

}
