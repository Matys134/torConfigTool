package com.school.torconfigtool;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileService {

    public void uploadFiles(MultipartFile[] files, String directory) throws IOException {
        for (MultipartFile file : files) {
            Path outputPath = Paths.get(directory, file.getOriginalFilename());
            try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
                fos.write(file.getBytes());
            }
        }
    }

    public void deleteFile(String fileName, String directory) throws FileDeletionException {
        Path filePath;
        if (fileName.startsWith(directory)) {
            filePath = Paths.get(fileName);
        } else {
            filePath = Paths.get(directory, fileName);
        }
        File fileToRemove = filePath.toFile();
        if (!fileToRemove.exists()) {
            System.out.println("File does not exist: " + fileName);
            return;
        }
        if (!fileToRemove.delete()) {
            throw new FileDeletionException("Failed to delete file: " + fileName);
        }
        if (fileToRemove.exists()) {
            System.out.println("File still exists after deletion attempt: " + fileName);
        }
    }

    public List<File> getUploadedFiles(String directory) {
        File folder = new File(directory);
        return Arrays.stream(folder.listFiles())
                .collect(Collectors.toList());
    }
}

class FileDeletionException extends Exception {
    public FileDeletionException(String message) {
        super(message);
    }
}