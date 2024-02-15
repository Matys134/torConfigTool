package com.school.torconfigtool;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Service class for handling file operations.
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
     * Uploads files to a specified directory and updates the model with the result.
     * @param files The files to be uploaded.
     * @param fileDir The directory where the files will be uploaded.
     * @param model The model to be updated with the result.
     * @return The name of the view to be rendered.
     */
    public String uploadFiles(MultipartFile[] files, String fileDir, Model model) {
        try {
            fileService.uploadFiles(files, fileDir);
            updateModelWithFiles(fileDir, model, "Files uploaded successfully!");
            return "file_upload_form";
        } catch (Exception e) {
            model.addAttribute("message", "Fail! -> uploaded filename: " + Arrays.toString(files));
            return "file_upload_form";
        }
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
            updateModelWithFiles(fileDir, model, "Files deleted successfully.");
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
     * @param fileDir The directory where the files are located.
     * @param model The model to be updated.
     * @param message The message to be added to the model.
     */
    private void updateModelWithFiles(String fileDir, Model model, String message) {
        List<File> fileNames = fileService.getUploadedFiles(fileDir);
        model.addAttribute("uploadedFiles", fileNames);
        model.addAttribute("message", message);
    }

    /**
     * Retrieves the list of uploaded files from a specified directory.
     *
     * @param fileDir The directory where the files are located.
     * @return The list of uploaded files.
     */
    public List<File> getUploadedFiles(String fileDir) {
        return fileService.getUploadedFiles(fileDir);
    }
}