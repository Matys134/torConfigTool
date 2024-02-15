package com.school.torconfigtool.file.controller;

import com.school.torconfigtool.file.service.FileOperationsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

/**
 * This class is a controller for file operations.
 * It handles requests related to file upload and removal.
 */
@Controller
@RequestMapping("/file")
public class FileController {

    private final FileOperationsService fileOperationsService;

    /**
     * Constructor for FileController.
     * @param fileOperationsService the service to handle file operations
     */
    public FileController(FileOperationsService fileOperationsService) {
        this.fileOperationsService = fileOperationsService;
    }

    /**
     * Handles the POST request to upload files.
     * @param files the files to be uploaded
     * @param port the port number
     * @param model the model to be used in the view
     * @return the view name
     */
    @PostMapping("/upload/{port}")
    public String uploadFiles(@RequestParam("files") MultipartFile[] files, @PathVariable("port") int port, Model model) {
        String fileDir = "onion/www/service-" + port + "/";
        return fileOperationsService.uploadFiles(files, fileDir, model);
    }

    /**
     * Handles the POST request to remove files.
     * @param fileNames the names of the files to be removed
     * @param port the port number
     * @param model the model to be used in the view
     * @return the view name
     */
    @PostMapping("/remove-files/{port}")
    public String removeFiles(@RequestParam("selectedFiles") String[] fileNames, @PathVariable("port") int port, Model model) {
        String fileDir = "onion/www/service-" + port + "/";
        return fileOperationsService.removeFiles(fileNames, fileDir, model);
    }

    /**
     * Handles the GET request to show the file upload form.
     * @param port the port number
     * @param model the model to be used in the view
     * @return the view name
     */
    @GetMapping("/upload/{port}")
    public String showUploadForm(@PathVariable("port") int port, Model model) {
        String uploadDir = "onion/www/service-" + port + "/";
        List<File> fileNames = fileOperationsService.getUploadedFiles(uploadDir);
        model.addAttribute("uploadedFiles", fileNames);
        return "file_upload_form";
    }
}