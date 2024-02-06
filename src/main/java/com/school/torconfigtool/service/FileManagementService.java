package com.school.torconfigtool.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class FileManagementService {

    Logger logger = LoggerFactory.getLogger(FileManagementService.class);

    @PostMapping("/upload/{port}")
    public String uploadFiles(@RequestParam("files") MultipartFile[] files, @PathVariable("port") int port, Model model) {
        try {
            Arrays.stream(files).forEach(file -> {
                String fileDir = "onion/www/service-" + port + "/";
                File outputFile = new File(fileDir + file.getOriginalFilename());

                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    fos.write(file.getBytes());
                } catch (IOException e) {
                    logger.error("Error during file saving", e);
                    throw new RuntimeException("Error during file saving: " + e.getMessage());
                }
            });

            List<String> fileNames = getUploadedFiles(port);
            model.addAttribute("uploadedFiles", fileNames);

            model.addAttribute("message", "Files uploaded successfully!");
            return "file_upload_form";
        } catch (Exception e) {
            model.addAttribute("message", "Fail! -> uploaded filename: " + Arrays.toString(files));
            return "file_upload_form";
        }
    }

    @PostMapping("/remove-file/{fileName}/{port}")
    public String removeFile(@PathVariable("fileName") String fileName, @PathVariable("port") int port, Model model) {
        try {
            String fileDir = "onion/www/service-" + port + "/";
            File fileToRemove = new File(fileDir + fileName);

            if (fileToRemove.exists()) {
                if (!fileToRemove.delete()) {
                    model.addAttribute("message", "Error deleting the file.");
                } else {
                    model.addAttribute("message", "File deleted successfully.");
                }
            } else {
                model.addAttribute("message", "File doesn't exist.");
            }
        } catch (Exception e) {
            model.addAttribute("message", "Error: " + e.getMessage());
        }

        List<String> fileNames = getUploadedFiles(port);
        model.addAttribute("uploadedFiles", fileNames);

        return "file_upload_form";
    }

    public List<String> getUploadedFiles(int port) {
        String uploadDir = "onion/www/service-" + port + "/";
        File folder = new File(uploadDir);
        return Arrays.asList(Objects.requireNonNull(folder.list()));
    }
}
