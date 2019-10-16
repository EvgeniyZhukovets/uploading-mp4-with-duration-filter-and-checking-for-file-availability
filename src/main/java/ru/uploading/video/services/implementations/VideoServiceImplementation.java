package ru.uploading.video.services.implementations;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.mp4parser.IsoFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.uploading.video.configuration.DefaultFileSavingPathConfiguration;
import ru.uploading.video.dto.ResponseDto;
import ru.uploading.video.services.VideoService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

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
        //get uploading file extension
        final String fileExtension = Objects.requireNonNull(FilenameUtils.getExtension(multipartFile.getOriginalFilename()));
        //generate new file`s name with extension
        final String fullFileName = fileName.toString() + "." + fileExtension;
        //path to temp folder
        final String pathToTempFolder = DefaultFileSavingPathConfiguration.SAVING_FOLDER + File.separator + fileName;
        //path to file in temp folder (temp folder is default saving folder + UUID from filename + fullFileName
        final String fullFilePathInTempFolder = pathToTempFolder + File.separator + fullFileName;

        final File uploadingFileInTempFolder = new File(fullFilePathInTempFolder);
        if (!multipartFile.getOriginalFilename().isEmpty()) {
            log.info("Saving video {}", fullFileName);
            final File defaultSavingFolder = DefaultFileSavingPathConfiguration.initSavingFolder();
            try (final InputStream inputStream = multipartFile.getInputStream()) {
                //uploading
                FileUtils.copyInputStreamToFile(inputStream, uploadingFileInTempFolder);

                //validating for duration
                isoFile = new IsoFile(uploadingFileInTempFolder);
                final double actualVideoDurationInSeconds = (double)
                        isoFile.getMovieBox().getMovieHeaderBox().getDuration() /
                        isoFile.getMovieBox().getMovieHeaderBox().getTimescale();
                if (actualVideoDurationInSeconds > maximumVideoDuration) {
                    deleteFolder(uploadingFileInTempFolder);
                    log.warn("Uploading file duration invalid. Expected : {} sec. Actual: {} sec.", maximumVideoDuration, actualVideoDurationInSeconds);
                    return new ResponseDto("Invalid duration", HttpStatus.BAD_REQUEST);
                }
                isoFile.close();

                //validating for file availability
                for (final File existingFile : Objects.requireNonNull(defaultSavingFolder.listFiles())) {
                    if (existingFile.isFile() && FileUtils.contentEquals(existingFile, uploadingFileInTempFolder)) {
                        deleteFolder(uploadingFileInTempFolder);
                        log.warn("Uploading file already exists. Existing file: {}", existingFile.getName());
                        return new ResponseDto("File already exists", HttpStatus.BAD_REQUEST);
                    }
                }

                //moving file from temp directory to default saving directory
                FileUtils.moveFileToDirectory(uploadingFileInTempFolder, new File(DefaultFileSavingPathConfiguration.SAVING_FOLDER), false);
                //delete temp folder
                deleteFolder(uploadingFileInTempFolder);
            } catch (final IOException e) {
                deleteFolder(uploadingFileInTempFolder);
                deleteFile(new File(DefaultFileSavingPathConfiguration.SAVING_FOLDER, fullFileName));
                log.error("Error saving file {}", fullFileName, e);
                return new ResponseDto("Error saving file", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            log.info("Uploaded file {}", fullFileName);
            return new ResponseDto("Uploaded. File name: " + fullFileName, HttpStatus.OK);
        } else {
            return new ResponseDto("Server error - empty file", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    /**
     * Deleting directory
     */
    private boolean deleteFolder(final File file) {
        return (file != null && file.exists() && file.isDirectory()) && file.delete();
    }

    /**
     * Deleting file
     */
    private boolean deleteFile(final File file) {
        return (file != null && file.exists() && file.isFile()) && file.delete();
    }
}
