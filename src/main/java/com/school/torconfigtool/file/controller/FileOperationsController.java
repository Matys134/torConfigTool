package com.school.torconfigtool.file.controller;

import com.school.torconfigtool.file.service.FileOperationsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * This class is a controller for file operations.
 * It handles requests related to file upload and removal.
 */
@Controller
@RequestMapping("/file")
public class FileOperationsController {

    private final FileOperationsService fileOperationsService;

    /**
     * Constructor for FileController.
     * @param fileOperationsService the service to handle file operations
     */
    public FileOperationsController(FileOperationsService fileOperationsService) {
        this.fileOperationsService = fileOperationsService;
    }

    /**
     * This method handles GET requests to show the file upload form.
     * @param port the port number
     * @param model the model object
     * @return a string representing the name of the view
     */
    @GetMapping("/upload/{port}")
    public String showUploadForm(@PathVariable("port") int port, Model model) {
        return fileOperationsService.showUploadForm(port, model);
    }

    /**
     * This method handles POST requests to remove selected files.
     * @param fileNames the names of the files to be removed
     * @param port the port number
     * @param model the model object
     * @return a string representing the name of the view
     */
    @PostMapping("/remove-files/{port}")
    public String removeFiles(@RequestParam("selectedFiles") String[] fileNames, @PathVariable("port") int port, Model model) {
        return fileOperationsService.removeFiles(fileNames, port, model);
    }

    /**
     * This method handles POST requests to upload files.
     * @param files the files to be uploaded
     * @param port the port number
     * @param model the model object
     * @return a string representing the name of the view
     */
    @PostMapping("/upload/{port}")
    public String uploadFiles(@RequestParam("files") MultipartFile[] files, @PathVariable("port") int port, Model model) {
        return fileOperationsService.uploadFiles(files, port, model);
    }
}