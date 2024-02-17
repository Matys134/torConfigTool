package com.school.torconfigtool;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Service
public class FileOperationsService {

    private final FileService fileService;

    public FileOperationsService(FileService fileService) {
        this.fileService = fileService;
    }

    public String uploadFiles(MultipartFile[] files, String fileDir, Model model) {
        try {
            fileService.uploadFiles(files, fileDir);
            List<String> fileNames = fileService.getUploadedFiles(fileDir);
            model.addAttribute("uploadedFiles", fileNames);
            model.addAttribute("message", "Files uploaded successfully!");
            return "file_upload_form";
        } catch (Exception e) {
            model.addAttribute("message", "Fail! -> uploaded filename: " + Arrays.toString(files));
            return "file_upload_form";
        }
    }

    public String removeFiles(String[] fileNames, String fileDir, Model model) {
        try {
            for (String fileName : fileNames) {
                fileService.deleteFile(fileName, fileDir);
            }
            List<String> remainingFileNames = fileService.getUploadedFiles(fileDir);
            model.addAttribute("uploadedFiles", remainingFileNames);
            model.addAttribute("message", "Files deleted successfully.");
            return "file_upload_form";
        } catch (Exception e) {
            model.addAttribute("message", "Error: " + e.getMessage());
            return "file_upload_form";
        }
    }

    public List<String> getUploadedFiles(String fileDir) {
        return fileService.getUploadedFiles(fileDir);
    }
}