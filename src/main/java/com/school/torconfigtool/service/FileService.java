package com.school.torconfigtool.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * FileService is a Spring Service that provides methods for file operations such as uploading and deleting files.
 */
@Service
public class FileService {

    public void uploadFiles(MultipartFile[] files, String directory) throws IOException {
        for (MultipartFile file : files) {
            Path outputPath = Paths.get(directory, file.getOriginalFilename());
            System.out.println("outputPath: " + outputPath.toFile().getAbsolutePath());
            try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
                fos.write(file.getBytes());
            }
        }
    }

    public void deleteFile(String[] fileNames, String directory) throws IOException {
        for (String fileName : fileNames) {
            Path fileToRemovePath = Paths.get(directory, fileName);
            File fileToRemove = fileToRemovePath.toFile();
            if (!fileToRemove.delete()) {
                throw new IOException("Failed to delete file: " + fileName);
            }
        }
    }

    public List<String> getUploadedFiles(String directory) {
        File folder = new File(directory);
        System.out.println("folder: " + folder.getAbsolutePath());
        return Arrays.asList(Objects.requireNonNull(folder.list()));
    }
}