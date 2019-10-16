package ru.uploading.video.services.implementations;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.mp4parser.IsoFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.uploading.video.configurations.DefaultFileSavingPathConfiguration;
import ru.uploading.video.dto.ResponseDto;
import ru.uploading.video.services.VideoService;
import ru.uploading.video.utils.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

@Log4j2
@Service
public class VideoServiceImplementation implements VideoService {

    /**
     * Maximum video duration parameter
     */
    @Value("#{new Double('${uploading.maximumVideoDurationInSeconds}')}")
    private Double maximumVideoDuration;

    /**
     * Default saving folder
     */
    private final File defaultSavingFolder = DefaultFileSavingPathConfiguration.initSavingFolder();

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
            try (final InputStream inputStream = multipartFile.getInputStream()) {
                //uploading
                FileUtils.copyInputStreamToFile(inputStream, uploadingFileInTempFolder);

                //validating for duration
                if (isDurationInvalid(uploadingFileInTempFolder))
                    return new ResponseDto("Invalid duration", HttpStatus.BAD_REQUEST);

                //validating for file availability
                if (isFileAlreadyExists(uploadingFileInTempFolder, defaultSavingFolder))
                    return new ResponseDto("File already exists", HttpStatus.BAD_REQUEST);

                //moving file from temp directory to default saving directory
                FileUtils.moveFileToDirectory(uploadingFileInTempFolder, new File(DefaultFileSavingPathConfiguration.SAVING_FOLDER), false);
                //delete temp folder
                FileUtil.safeFolderDelete(uploadingFileInTempFolder);
            } catch (final IOException e) {
                FileUtil.safeFolderDelete(uploadingFileInTempFolder);
                FileUtil.safeFileDelete(new File(DefaultFileSavingPathConfiguration.SAVING_FOLDER, fullFileName));
                log.error("Error saving file {}", fullFileName, e);
                return new ResponseDto("Error saving file", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            log.info("Uploaded file {}", fullFileName);
            return new ResponseDto("Uploaded. File name: " + fullFileName, HttpStatus.OK);
        } else {
            return new ResponseDto("Server error - empty file", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private boolean isFileAlreadyExists(final File uploadingFileInTempFolder, final File defaultSavingFolder) throws IOException {
        for (final File existingFile : Objects.requireNonNull(defaultSavingFolder.listFiles())) {
            if (existingFile.isFile() && FileUtils.contentEquals(existingFile, uploadingFileInTempFolder)) {
                FileUtil.safeFolderDelete(uploadingFileInTempFolder);
                log.warn("Uploading file already exists. Existing file: {}", existingFile.getName());
                return true;
            }
        }
        return false;
    }

    private boolean isDurationInvalid(final File uploadingFileInTempFolder) throws IOException {
        //IsoParser container to gather info about MP4 file
        final IsoFile isoFile = new IsoFile(uploadingFileInTempFolder);
        final double actualVideoDurationInSeconds = (double)
                isoFile.getMovieBox().getMovieHeaderBox().getDuration() /
                isoFile.getMovieBox().getMovieHeaderBox().getTimescale();
        if (actualVideoDurationInSeconds > maximumVideoDuration) {
            FileUtil.safeFolderDelete(uploadingFileInTempFolder);
            log.warn("Uploading file duration invalid. Expected : {} sec. Actual: {} sec.", maximumVideoDuration, actualVideoDurationInSeconds);
            return true;
        }
        isoFile.close();
        return false;
    }
}
