package ru.itis.test.services.implementations;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.mp4parser.IsoFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.itis.test.configuration.DefaultFileSavingPathConfiguration;
import ru.itis.test.services.VideoService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

import static ru.itis.test.configuration.DefaultFileSavingPathConfiguration.SAVING_FOLDER;

@Service
public class VideoServiceImplementation implements VideoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VideoServiceImplementation.class);

    private IsoFile isoFile;

    @Value("#{new Double('${uploading.maximumVideoDurationInSeconds}')}")
    private Double maximumVideoDuration;

    @Override
    public String uploadVideo(final MultipartFile multipartFile) {
        //generate random file name
        final UUID fileName = UUID.randomUUID();
        final String fileExtension = Objects.requireNonNull(FilenameUtils.getExtension(multipartFile.getOriginalFilename()));
        final String fullFileName = fileName.toString() + "." + fileExtension;
        final String fullFilePath = SAVING_FOLDER + File.separator + fullFileName;

        final File newFile = new File(fullFilePath);
        if (!multipartFile.getOriginalFilename().isEmpty()) {
            LOGGER.info("Saving video {}", fullFileName);
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
                    LOGGER.warn("Uploading file duration invalid. Expected : {} sec. Actual: {} sec.", maximumVideoDuration, actualVideoDurationInSeconds);
                    return "Invalid duration";
                }

                //validating for file availability
                for (final File existingFile : Objects.requireNonNull(defaultSavingFolder.listFiles())) {
                    if (FileUtils.contentEquals(existingFile, newFile)) {
                        deleteFile(newFile);
                        LOGGER.warn("Uploading file already exists. Existing file: {}", existingFile.getName());
                        return "File already exists";
                    }
                }

            } catch (final IOException e) {
                LOGGER.error("Error saving file {}", fullFileName, e);
                return "Error saving file";
            }

            LOGGER.info("Uploaded file {}", fileName);
            return "OK";
        } else {
            return "Server error - empty file";
        }

    }

    private boolean deleteFile(final File file) {
        return (file != null && file.exists() && file.isFile()) && file.delete();
    }
}
