package ru.itis.test.services.implementations;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.mp4parser.IsoFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.itis.test.configuration.DefaultFileSavingPathConfiguration;
import ru.itis.test.dto.ResponseDto;
import ru.itis.test.services.VideoService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

import static ru.itis.test.configuration.DefaultFileSavingPathConfiguration.SAVING_FOLDER;

@Log4j2
@Service
public class VideoServiceImplementation implements VideoService {

    /**
     * IsoParser container to gather info about MP4 file
     */
    private IsoFile isoFile;

    /**
     * Maximum video duration parameter
     */
    @Value("#{new Double('${uploading.maximumVideoDurationInSeconds}')}")
    private Double maximumVideoDuration;

    /**
     * Uploading video business logic
     */
    @Override
    public ResponseDto uploadVideo(final MultipartFile multipartFile) {
        //generate random file name
        final UUID fileName = UUID.randomUUID();
        final String fileExtension = Objects.requireNonNull(FilenameUtils.getExtension(multipartFile.getOriginalFilename()));
        final String fullFileName = fileName.toString() + "." + fileExtension;
        final String fullFilePath = SAVING_FOLDER + File.separator + fullFileName;

        final File newFile = new File(fullFilePath);
        if (!multipartFile.getOriginalFilename().isEmpty()) {
            log.info("Saving video {}", fullFileName);
            final File defaultSavingFolder = DefaultFileSavingPathConfiguration.initSavingFolder();
            try (final InputStream inputStream = multipartFile.getInputStream()) {
                //uploading
                FileUtils.copyInputStreamToFile(inputStream, newFile);

                //validating for duration
                isoFile = new IsoFile(fullFilePath);
                final double actualVideoDurationInSeconds = (double)
                        isoFile.getMovieBox().getMovieHeaderBox().getDuration() /
                        isoFile.getMovieBox().getMovieHeaderBox().getTimescale();
                if (actualVideoDurationInSeconds > maximumVideoDuration) {
                    deleteFile(newFile);
                    log.warn("Uploading file duration invalid. Expected : {} sec. Actual: {} sec.", maximumVideoDuration, actualVideoDurationInSeconds);
                    return new ResponseDto("Invalid duration", HttpStatus.BAD_REQUEST);
                }

                //validating for file availability
                for (final File existingFile : Objects.requireNonNull(defaultSavingFolder.listFiles())) {
                    if (FileUtils.contentEquals(existingFile, newFile)) {
                        deleteFile(newFile);
                        log.warn("Uploading file already exists. Existing file: {}", existingFile.getName());
                        return new ResponseDto("File already exists", HttpStatus.BAD_REQUEST);
                    }
                }

            } catch (final IOException e) {
                log.error("Error saving file {}", fullFileName, e);
                return new ResponseDto("Error saving file", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            log.info("Uploaded file {}", fileName);
            return new ResponseDto("Uploaded. File name: " + fullFileName, HttpStatus.OK);
        } else {
            return new ResponseDto("Server error - empty file", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    /**
     * Deleting file logic
     */
    private boolean deleteFile(final File file) {
        return (file != null && file.exists() && file.isFile()) && file.delete();
    }
}
