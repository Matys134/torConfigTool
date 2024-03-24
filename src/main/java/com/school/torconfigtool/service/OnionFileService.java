package com.school.torconfigtool.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * FileService is a Spring Service that provides methods for file operations such as uploading and deleting files.
 */
@Service
public class OnionFileService {

    /**
     * Uploads an array of files to a specified directory.
     *
     * @param files the files to be uploaded
     * @param directory the directory where the files will be uploaded
     * @throws IOException if an I/O error occurs during file upload
     */
    public void uploadFiles(MultipartFile[] files, String directory) throws IOException {
        for (MultipartFile file : files) {
            File outputFile = new File(directory + file.getOriginalFilename());
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(file.getBytes());
            }
        }
    }

    /**
     * Deletes a file from a specified directory.
     *
     * @param fileName the name of the file to be deleted
     * @param directory the directory where the file is located
     * @throws IOException if an I/O error occurs during file deletion
     */
    public void deleteFile(String fileName, String directory) throws IOException {
        File fileToRemove = new File(directory + fileName);
        if (!fileToRemove.delete()) {
            throw new IOException("Failed to delete file: " + fileName);
        }
    }

    /**
     * Retrieves the names of all files in a specified directory.
     *
     * @param directory the directory from which to retrieve file names
     * @return a list of file names
     */
    public List<String> getUploadedFilesFromDirectory(String directory) {
        File folder = new File(directory);
        return Arrays.asList(Objects.requireNonNull(folder.list()));
    }

    /**
     * Retrieves the names of all files uploaded to a specific port.
     *
     * @param port the port from which to retrieve file names
     * @return a list of file names
     */
    public List<String> getUploadedFilesFromPort(int port) {
        String uploadDir = "onion/www/service-" + port + "/";
        return getUploadedFilesFromDirectory(uploadDir);
    }

    /**
     * Uploads an array of files to a specific port and returns the names of all files in the directory.
     *
     * @param files the files to be uploaded
     * @param port the port where the files will be uploaded
     * @return a list of file names
     * @throws IOException if an I/O error occurs during file upload
     */
    public List<String> uploadFilesToPort(MultipartFile[] files, int port) throws IOException {
        String fileDir = "onion/www/service-" + port + "/";
        uploadFiles(files, fileDir);
        return getUploadedFilesFromDirectory(fileDir);
    }

    /**
     * Deletes an array of files from a specific port.
     *
     * @param fileNames the names of the files to be deleted
     * @param port the port where the files will be deleted from
     * @throws IOException if an I/O error occurs during file deletion
     */
    public void deleteFilesFromPort(String[] fileNames, int port) throws IOException {
        String fileDir = "onion/www/service-" + port + "/";
        for (String fileName : fileNames) {
            deleteFile(fileName, fileDir);
        }
    }
}