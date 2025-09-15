package com.boostbench.api.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class FileUploadUtil {

    @Value("${file.upload-dir}")
    private String uploadDir;

    // Allowed file extensions
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"
    );

    // Maximum file size (10MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * Uploads a new file with a unique filename.
     */
    public String uploadFile(MultipartFile file) throws IOException {
        validateFile(file);

        String originalFileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFileName);
        String fileName = UUID.randomUUID() + fileExtension;

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            System.out.println("Created upload directory: " + uploadPath);
        }

        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // URL-encode the filename for the relative path
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
        String relativePath = "/uploads/" + encodedFileName;
        System.out.println("File uploaded successfully:");
        System.out.println("Original name: " + originalFileName);
        System.out.println("Saved as: " + fileName);
        System.out.println("Relative path: " + relativePath);
        System.out.println("Full path: " + filePath.toString());

        return relativePath;
    }

    /**
     * Updates an existing file by overwriting it with the new file content.
     * @param file The new file to upload
     * @param existingFilePath The relative path of the file to update (e.g., "/uploads/filename")
     * @return The path of the updated file
     * @throws IOException if the file operation fails
     */
    public String updateFile(MultipartFile file, String existingFilePath) throws IOException {
        validateFile(file);

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Extract filename from the existing path
        String fileName = existingFilePath.replace("/uploads/", "");
        Path filePath = uploadPath.resolve(fileName);

        if (!Files.exists(filePath)) {
            System.out.println("File not found, creating new: " + existingFilePath);
            return uploadFile(file); // Create new file if existing doesn't exist
        }

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // URL-encode the filename for the relative path
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
        String relativePath = "/uploads/" + encodedFileName;
        System.out.println("File updated successfully: " + relativePath);

        return relativePath;
    }

    /**
     * Deletes a file from the upload directory
     * @param filePath The relative path of the file to delete
     * @return true if file was deleted, false otherwise
     */
    public boolean deleteFile(String filePath) {
        try {
            String fileName = filePath.replace("/uploads/", "");
            // Decode the filename to match the actual file on disk
            String decodedFileName = java.net.URLDecoder.decode(fileName, StandardCharsets.UTF_8.toString());
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
            Path fileToDelete = uploadPath.resolve(decodedFileName);

            if (Files.exists(fileToDelete)) {
                Files.delete(fileToDelete);
                System.out.println("File deleted: " + filePath);
                return true;
            } else {
                System.out.println("File not found for deletion: " + filePath);
                return false;
            }
        } catch (IOException e) {
            System.err.println("Error deleting file " + filePath + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Validates the uploaded file
     */
    private void validateFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IOException("File size exceeds maximum limit of " + (MAX_FILE_SIZE / 1024 / 1024) + "MB");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty()) {
            throw new IOException("Invalid file name");
        }

        String fileExtension = getFileExtension(originalFileName).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
            throw new IOException("File type not allowed. Allowed types: " + ALLOWED_EXTENSIONS);
        }

        // Validate content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("Invalid file content type. Expected image file.");
        }
    }

    /**
     * Extracts file extension from filename
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex).toLowerCase();
        }

        return "";
    }

    /**
     * Gets the absolute upload directory path
     */
    public String getUploadDirectory() {
        return Paths.get(uploadDir).toAbsolutePath().toString();
    }
}