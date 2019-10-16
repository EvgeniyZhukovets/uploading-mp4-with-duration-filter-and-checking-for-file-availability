package ru.itis.test.configuration;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Log4j2
@Configuration
public class DefaultFileSavingPathConfiguration {

    public static final String SAVING_FOLDER = System.getProperty("java.io.tmpdir");

    @Bean
    public static File initSavingFolder() {
        final File file = new File(SAVING_FOLDER);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdir();
        }
        log.info("Default saving folder: {}", file.getAbsoluteFile());
        return file;
    }
}