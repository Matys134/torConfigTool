package com.school.torconfigtool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for handling file operations such as uploading and deleting files.
 */
@Service
public class FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    /**
     * Uploads multiple files to a specified directory.
     *
     * @param files     The files to be uploaded.
     * @param directory The directory where the files will be uploaded.
     * @throws IOException If an I/O error occurs during file upload.
     */
    public void uploadFiles(MultipartFile[] files, String directory) throws IOException {
        for (MultipartFile file : files) {
            Path outputPath = Paths.get(directory, file.getOriginalFilename());
            try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
                fos.write(file.getBytes());
            }
        }
    }

    /**
     * Deletes a file from a specified directory.
     *
     * @param fileName  The name of the file to be deleted.
     * @param directory The directory where the file is located.
     * @throws IOException If the file does not exist or an I/O error occurs during file deletion.
     */
    public void deleteFile(String fileName, String directory) throws IOException {
        Path filePath;
        if (fileName.startsWith(directory)) {
            filePath = Paths.get(fileName);
        } else {
            filePath = Paths.get(directory, fileName);
        }
        File fileToRemove = filePath.toFile();
        if (!fileToRemove.exists()) {
            throw new IOException("File does not exist: " + fileName);
        }
        if (!fileToRemove.delete()) {
            throw new IOException("Failed to delete file: " + fileName);
        }
    }

    /**
     * Retrieves a list of all uploaded files in a specified directory.
     *
     * @param directory The directory from which to retrieve the files.
     * @return A list of File objects representing the uploaded files. If the directory does not exist or an I/O error occurs, an empty list is returned.
     */
    public List<File> getUploadedFiles(String directory) {
        File folder = new File(directory);
        File[] files = folder.listFiles();
        if (files != null) {
            return Arrays.stream(files)
                    .collect(Collectors.toList());
        } else {
            logger.error("Directory does not exist or an I/O error occurred: " + directory);
            return Collections.emptyList();
        }
    }
}