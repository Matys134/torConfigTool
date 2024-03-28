package com.school.torconfigtool.controller;

import com.school.torconfigtool.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/file")
public class FileController {

    private final FileService fileService;
    private final String baseDirectory = "/onion/www/service-";

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload/8555")
    public String uploadFiles(@RequestParam("files") MultipartFile[] files,
                              Model model) {
        try {
            String directory = baseDirectory + 8555;
            fileService.uploadFiles(files, directory);
            List<String> fileNames = fileService.getUploadedFiles(directory);
            model.addAttribute("uploadedFiles", fileNames);
            model.addAttribute("message", "Files uploaded successfully!");
        } catch (Exception e) {
            model.addAttribute("message", "Fail! -> uploaded filename: "
                    + Arrays.toString(files));
        }
        return "file_upload_form";
    }

    @PostMapping("/remove-files/8555")
    public String removeFiles(@RequestParam("selectedFiles") String[] fileNames,
                              Model model) {
        try {
            String directory = baseDirectory + 8555;
            fileService.deleteFile(fileNames, directory);
            List<String> remainingFileNames = fileService.getUploadedFiles(directory);
            model.addAttribute("uploadedFiles", remainingFileNames);
            model.addAttribute("message", "Files deleted successfully.");
        } catch (Exception e) {
            model.addAttribute("message", "Error: " + e.getMessage());
        }
        return "file_upload_form";
    }

    @GetMapping("/upload/8555")
    public String showUploadForm(Model model) {
        String directory = baseDirectory + 8555;
        List<String> fileNames = fileService.getUploadedFiles(directory);
        model.addAttribute("uploadedFiles", fileNames);
        return "file_upload_form";
    }
}