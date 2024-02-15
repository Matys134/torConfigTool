package com.school.torconfigtool.file.service;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Service class for handling file operations.
 * This class provides methods for uploading and deleting files in a specified directory.
 * It also updates the model with the result of these operations.
 */
@Service
public class FileOperationsService {

    private final FileService fileService;

    /**
     * Constructor for FileOperationsService.
     * @param fileService The service for handling file operations.
     */
    public FileOperationsService(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * Removes files from a specified directory and updates the model with the result.
     * @param fileNames The names of the files to be removed.
     * @param fileDir The directory where the files are located.
     * @param model The model to be updated with the result.
     * @return The name of the view to be rendered.
     */
    public String removeFiles(String[] fileNames, String fileDir, Model model) {
        try {
            deleteFiles(fileNames, fileDir);
            updateModelWithFiles(fileDir, model);
            return "file_upload_form";
        } catch (Exception e) {
            model.addAttribute("message", "Error: " + e.getMessage());
            return "file_upload_form";
        }
    }

    /**
     * Deletes files from a specified directory.
     * @param fileNames The names of the files to be deleted.
     * @param fileDir The directory where the files are located.
     * @throws Exception If an error occurs during file deletion.
     */
    private void deleteFiles(String[] fileNames, String fileDir) throws Exception {
        for (String fileName : fileNames) {
            fileService.deleteFile(fileName, fileDir);
        }
    }

    /**
     * Updates the model with the list of uploaded files and a message.
     *
     * @param fileDir The directory where the files are located.
     * @param model   The model to be updated.
     */
    private void updateModelWithFiles(String fileDir, Model model) {
        List<File> fileNames = fileService.getUploadedFiles(fileDir);
        model.addAttribute("uploadedFiles", fileNames);
        model.addAttribute("message", "Files deleted successfully.");
    }

    /**
     * Uploads files to a specified directory and updates the model with the result.
     * @param files The files to be uploaded.
     * @param port The port number used to construct the directory path.
     * @param model The model to be updated with the result.
     * @return The name of the view to be rendered.
     */
    public String uploadFiles(@RequestParam("files") MultipartFile[] files, @PathVariable("port") int port, Model model) {
        try {
            String fileDir = "onion/www/service-" + port + "/";
            fileService.uploadFiles(files, fileDir);
            List<File> fileNames = fileService.getUploadedFiles(fileDir);
            model.addAttribute("uploadedFiles", fileNames);
            model.addAttribute("message", "Files uploaded successfully!");
            return "file_upload_form";
        } catch (Exception e) {
            model.addAttribute("message", "Fail! -> uploaded filename: " + Arrays.toString(files));
            return "file_upload_form";
        }
    }

    /**
     * Removes files from a specified directory based on the port number and updates the model with the result.
     * @param fileNames The names of the files to be removed.
     * @param port The port number used to construct the directory path.
     * @param model The model to be updated with the result.
     * @return The name of the view to be rendered.
     */
    public String removeFiles(@RequestParam("selectedFiles") String[] fileNames, @PathVariable("port") int port, Model model) {
        String fileDir = "onion/www/service-" + port + "/";
        return removeFiles(fileNames, fileDir, model);
    }

    /**
     * Shows the upload form and updates the model with the list of uploaded files.
     * @param port The port number used to construct the directory path.
     * @param model The model to be updated with the list of uploaded files.
     * @return The name of the view to be rendered.
     */
    public String showUploadForm(@PathVariable("port") int port, Model model) {
        List<File> fileNames = getUploadedFiles(port);
        model.addAttribute("uploadedFiles", fileNames);
        return "file_upload_form";
    }

    /**
     * Retrieves the list of uploaded files from a specified directory.
     * @param port The port number used to construct the directory path.
     * @return The list of uploaded files.
     */
    private List<File> getUploadedFiles(int port) {
        String uploadDir = "onion/www/service-" + port + "/";
        return fileService.getUploadedFiles(uploadDir);
    }
}