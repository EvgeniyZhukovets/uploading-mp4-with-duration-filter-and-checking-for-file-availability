package ru.itis.test.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class DefaultFileSavingPathConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultFileSavingPathConfiguration.class);

    public static final String SAVING_FOLDER = System.getProperty("java.io.tmpdir");

    @Bean
    public static File initSavingFolder() {
        final File file = new File(SAVING_FOLDER);
        if(!file.exists() || !file.isDirectory()){
            file.mkdir();
        }
        LOGGER.info("Default saving folder: {}", file.getAbsoluteFile());
        return file;
    }

}
