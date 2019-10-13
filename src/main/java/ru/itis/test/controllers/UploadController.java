package ru.itis.test.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.itis.test.services.VideoService;

import java.util.Objects;


@RestController
public class UploadController {

    @Autowired
    private VideoService videoService;

    private final String MP4_CONTENT_TYPE = "video/mp4";

    @PostMapping("/uploadVideo")
    public ResponseEntity<?> uploadVideo(
            @RequestParam("video") final MultipartFile multipartFile) {
        if (!Objects.equals(multipartFile.getContentType(), MP4_CONTENT_TYPE)) {
            return new ResponseEntity<>("Invalid content-type", new HttpHeaders(), HttpStatus.OK);
        }
        return new ResponseEntity<>(videoService.uploadVideo(multipartFile), new HttpHeaders(), HttpStatus.OK);
    }

}
