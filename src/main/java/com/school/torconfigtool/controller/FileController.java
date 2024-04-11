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
 * FileController is a Spring MVC Controller that handles file upload and deletion operations.
 */
@Controller
@RequestMapping("/file")
public class FileController {

    private final FileService fileService;
    private final String baseDirectory = System.getProperty("user.dir") + "/onion/www/service-";

    /**
     * Constructor for FileController.
     * @param fileService The service to handle file operations.
     */
    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * Handles the POST request to upload files.
     * @param files The files to be uploaded.
     * @param port The port number used to create a unique directory for each service.
     * @param model The Model object to be returned to the view.
     * @return The name of the view to be rendered.
     */
    @PostMapping("/upload/{port}")
    public String uploadFiles(@RequestParam("files") MultipartFile[] files, @PathVariable("port") int port,
                              Model model) {
        try {
            String directory = baseDirectory + port + "/";
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

    /**
     * Handles the POST request to delete selected files.
     * @param fileNames The names of the files to be deleted.
     * @param port The port number used to create a unique directory for each service.
     * @param model The Model object to be returned to the view.
     * @return The name of the view to be rendered.
     */
    @PostMapping("/remove-files/{port}")
    public String removeFiles(@RequestParam("selectedFiles") String[] fileNames, @PathVariable("port") int port,
                              Model model) {
        try {
            String directory = baseDirectory + port + "/";
            fileService.deleteFile(fileNames, directory);
            List<String> remainingFileNames = fileService.getUploadedFiles(directory);
            model.addAttribute("uploadedFiles", remainingFileNames);
            model.addAttribute("message", "Files deleted successfully.");
        } catch (Exception e) {
            model.addAttribute("message", "Error: " + e.getMessage());
        }
        return "file_upload_form";
    }

    /**
     * Handles the GET request to show the file upload form.
     * @param port The port number used to create a unique directory for each service.
     * @param model The Model object to be returned to the view.
     * @return The name of the view to be rendered.
     */
    @GetMapping("/upload/{port}")
    public String showUploadForm(@PathVariable("port") int port, Model model) {
        String directory = baseDirectory + port + "/";
        List<String> fileNames = fileService.getUploadedFiles(directory);
        model.addAttribute("uploadedFiles", fileNames);
        return "file_upload_form";
    }
}