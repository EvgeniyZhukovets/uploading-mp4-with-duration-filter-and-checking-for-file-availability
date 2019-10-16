package ru.uploading.video.utils;

import lombok.experimental.UtilityClass;

import java.io.File;

@UtilityClass
public class FileUtil {

    /**
     * Deleting directory
     */
    public boolean safeFolderDelete(final File file) {
        return (file != null && file.exists() && file.isDirectory()) && file.delete();
    }

    /**
     * Deleting file
     */
    public boolean safeFileDelete(final File file) {
        return (file != null && file.exists() && file.isFile()) && file.delete();
    }
}
