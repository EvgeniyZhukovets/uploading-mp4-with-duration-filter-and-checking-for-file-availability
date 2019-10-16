package ru.itis.test.controllers;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.itis.test.dto.ResponseDto;
import ru.itis.test.services.VideoService;

import java.util.Objects;

@RestController
@RequestMapping(UploadController.BASE_UPLOADING_PATH)
public class UploadController {

    private VideoService videoService;

    /**
     * Constructor for {@link UploadController}. Injects all necessary dependencies
     *
     * @param videoService video service
     */
    @Autowired
    public UploadController(final VideoService videoService) {
        this.videoService = videoService;
    }

    /**
     * Base path for this controller
     */
    public static final String BASE_UPLOADING_PATH = "/api/v1/upload";

    /**
     * MP4 Content-Type
     */
    private final String MP4_CONTENT_TYPE = "video/mp4";

    /**
     * Uploading video endpoint
     * <p>
     * ApiOperation - documentation for Swagger
     */
    @PostMapping(value = "/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation(value = "Controller for uploading video",
            notes = "MP4 file format only")
    public ResponseEntity<?> uploadVideo(
            @RequestParam("video") final MultipartFile multipartFile) {
        if (!Objects.equals(multipartFile.getContentType(), MP4_CONTENT_TYPE)) {
            return new ResponseEntity<>("Invalid content-type", new HttpHeaders(), HttpStatus.BAD_REQUEST);
        }
        final ResponseDto responseDto = videoService.uploadVideo(multipartFile);
        return new ResponseEntity<>(responseDto, new HttpHeaders(), responseDto.getHttpStatus());
    }
}