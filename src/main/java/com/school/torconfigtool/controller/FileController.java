package com.school.torconfigtool.controller;

import com.school.torconfigtool.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * FileController is a Spring MVC Controller that handles HTTP requests related to file operations.
 * It uses FileService to perform the actual operations.
 */
@Controller
@RequestMapping("/file")
public class FileController {

    private final FileService fileService;

    /**
     * Constructs a new FileController with the provided FileService.
     *
     * @param fileService the FileService to be used for file operations
     */
    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * Handles POST requests to upload files to a specific port.
     *
     * @param files the files to be uploaded
     * @param port the port where the files will be uploaded to
     * @param model the Model object to be used for passing attributes to the view
     * @return the name of the view to be rendered
     */
    @PostMapping("/upload/{port}")
    public String uploadFiles(@RequestParam("files") MultipartFile[] files, @PathVariable("port") int port, Model model) {
        try {
            List<String> fileNames = fileService.uploadFilesToPort(files, port);
            model.addAttribute("uploadedFiles", fileNames);
            model.addAttribute("message", "Files uploaded successfully!");
        } catch (Exception e) {
            model.addAttribute("message", "Fail! -> uploaded filename: " + Arrays.toString(files));
        }
        return "file_upload_form";
    }

    /**
     * Handles POST requests to remove selected files from a specific port.
     *
     * @param fileNames the names of the files to be removed
     * @param port the port where the files will be removed from
     * @param model the Model object to be used for passing attributes to the view
     * @return the name of the view to be rendered
     */
    @PostMapping("/remove-files/{port}")
    public String removeFiles(@RequestParam("selectedFiles") String[] fileNames, @PathVariable("port") int port, Model model) {
        try {
            fileService.deleteFilesFromPort(fileNames, port);
            List<String> remainingFileNames = fileService.getUploadedFilesFromPort(port);
            model.addAttribute("uploadedFiles", remainingFileNames);
            model.addAttribute("message", "Files deleted successfully.");
        } catch (Exception e) {
            model.addAttribute("message", "Error: " + e.getMessage());
        }
        return "file_upload_form";
    }

    /**
     * Handles GET requests to show the file upload form for a specific port.
     *
     * @param port the port for which the file upload form will be shown
     * @param model the Model object to be used for passing attributes to the view
     * @return the name of the view to be rendered
     */
    @GetMapping("/upload/{port}")
    public String showUploadForm(@PathVariable("port") int port, Model model) {
        List<String> fileNames = fileService.getUploadedFilesFromPort(port);
        model.addAttribute("uploadedFiles", fileNames);
        return "file_upload_form";
    }
}