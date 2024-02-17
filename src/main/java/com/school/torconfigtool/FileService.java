package com.school.torconfigtool;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class FileService {

    public void uploadFiles(MultipartFile[] files, String directory) throws IOException {
        for (MultipartFile file : files) {
            File outputFile = new File(directory + file.getOriginalFilename());
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(file.getBytes());
            }
        }
    }

    public void deleteFile(String fileName, String directory) throws IOException {
        File fileToRemove = new File(directory + fileName);
        if (!fileToRemove.delete()) {
            throw new IOException("Failed to delete file: " + fileName);
        }
    }

    public List<String> getUploadedFiles(String directory) {
        File folder = new File(directory);
        return Arrays.asList(Objects.requireNonNull(folder.list()));
    }
}