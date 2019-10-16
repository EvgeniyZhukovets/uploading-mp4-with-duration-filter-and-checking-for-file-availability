package ru.uploading.video.utils;

import lombok.experimental.UtilityClass;

import java.io.File;

@UtilityClass
public class FileUtil {
    /**
     * Deleting directory
     */
    public boolean deleteFolder(final File file) {
        return (file != null && file.exists() && file.isDirectory()) && file.delete();
    }

    /**
     * Deleting file
     */
    public boolean deleteFile(final File file) {
        return (file != null && file.exists() && file.isFile()) && file.delete();
    }
}
