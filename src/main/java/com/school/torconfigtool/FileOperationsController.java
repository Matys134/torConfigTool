package com.school.torconfigtool;

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


    @GetMapping("/upload/{port}")
    public String showUploadForm(@PathVariable("port") int port, Model model) {
        return fileOperationsService.showUploadForm(port, model);
    }

    @PostMapping("/remove-files/{port}")
    public String removeFiles(@RequestParam("selectedFiles") String[] fileNames, @PathVariable("port") int port, Model model) {
        return fileOperationsService.removeFiles(fileNames, port, model);
    }

    @PostMapping("/upload/{port}")
    public String uploadFiles(@RequestParam("files") MultipartFile[] files, @PathVariable("port") int port, Model model) {
        return fileOperationsService.uploadFiles(files, port, model);
    }
}