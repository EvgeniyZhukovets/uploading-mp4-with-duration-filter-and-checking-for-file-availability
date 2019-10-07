package ru.itis.test.services.implementations;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.itis.test.configuration.DefaultFileSavingPathConfiguration;
import ru.itis.test.services.VideoService;
import com.xuggle.xuggler.IContainer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;

import static ru.itis.test.configuration.DefaultFileSavingPathConfiguration.SAVING_FOLDER;

@Service
public class VideoServiceImplementation implements VideoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VideoServiceImplementation.class);

    @Override
    public File uploadVideo(final MultipartFile multipartFile) {
        final UUID fileName = UUID.randomUUID();
        final String fileExtension = Objects.requireNonNull(FilenameUtils.getExtension(multipartFile.getOriginalFilename()));
        final String fullFileName = fileName.toString() + "." + fileExtension;

        final File newFile = new File(SAVING_FOLDER, fullFileName);
        if (!multipartFile.getOriginalFilename().isEmpty()) {
            LOGGER.info("Saving video {} to LOCAL_STORAGE", fullFileName);
            //save file to local storage
            final File defaultSavingFolder = DefaultFileSavingPathConfiguration.initSavingFolder();
            try (final InputStream inputStream = multipartFile.getInputStream()) {
                FileUtils.copyInputStreamToFile(inputStream, newFile);

                IContainer container = IContainer.make();
                for (final File existingFile : Objects.requireNonNull(defaultSavingFolder.listFiles())) {
                    if (FileUtils.contentEquals(existingFile, newFile)) {
                        deleteFile(newFile);
                        return null;
                    }
                }

            } catch (final IOException e) {
                LOGGER.error("Error saving file {}", fullFileName);
                return null;
            }

            return newFile;
        } else {
            return null;
        }


    }

    private boolean deleteFile(File file) {
        return (file != null && file.exists() && file.isFile()) && file.delete();
    }
}
