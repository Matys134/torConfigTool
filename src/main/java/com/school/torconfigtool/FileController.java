package com.school.torconfigtool;

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

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload/{port}")
    public String uploadFiles(@RequestParam("files") MultipartFile[] files, @PathVariable("port") int port, Model model) {
        try {
            String fileDir = "onion/www/service-" + port + "/";
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

    @PostMapping("/remove-files/{port}")
    public String removeFiles(@RequestParam("selectedFiles") String[] fileNames, @PathVariable("port") int port, Model model) {
        try {
            String fileDir = "onion/www/service-" + port + "/";
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

    @GetMapping("/upload/{port}")
    public String showUploadForm(@PathVariable("port") int port, Model model) {
        List<String> fileNames = getUploadedFiles(port);
        model.addAttribute("uploadedFiles", fileNames);
        return "file_upload_form";
    }

    private List<String> getUploadedFiles(int port) {
        String uploadDir = "onion/www/service-" + port + "/";
        return fileService.getUploadedFiles(uploadDir);
    }
}
