package com.pharmacy.management.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class PrescriptionFileStorageService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "pdf");

    private static final Path UPLOAD_DIRECTORY =
            Path.of("uploads", "prescriptions")
                    .toAbsolutePath()
                    .normalize();

    public String store(MultipartFile file) {

        System.out.println("=== PRESCRIPTION UPLOAD START ===");
        System.out.println("UPLOAD DIRECTORY = " + UPLOAD_DIRECTORY);

        if (file != null) {
            System.out.println("FILE NAME = " + file.getOriginalFilename());
            System.out.println("FILE SIZE = " + file.getSize());
            System.out.println("CONTENT TYPE = " + file.getContentType());
        }

        validate(file);

        String extension = extensionOf(file.getOriginalFilename());
        String storedFileName = UUID.randomUUID() + "." + extension;

        Path target = UPLOAD_DIRECTORY
                .resolve(storedFileName)
                .normalize();

        System.out.println("TARGET FILE = " + target);

        if (!target.startsWith(UPLOAD_DIRECTORY)) {
            throw new IllegalArgumentException("Invalid file name.");
        }

        try {

            Files.createDirectories(UPLOAD_DIRECTORY);

            System.out.println(
                    "DIRECTORY EXISTS = " +
                            Files.exists(UPLOAD_DIRECTORY)
            );

            try (InputStream inputStream = file.getInputStream()) {

                Files.copy(
                        inputStream,
                        target,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            System.out.println("FILE SAVED SUCCESSFULLY");
            System.out.println("SAVED FILE = " + target);
            System.out.println("=== PRESCRIPTION UPLOAD END ===");

            return "/uploads/prescriptions/" + storedFileName;

        } catch (IOException exception) {

            System.out.println("FILE SAVE ERROR");
            exception.printStackTrace();

            throw new IllegalStateException(
                    "The prescription file could not be saved. Please try again.",
                    exception
            );
        }
    }

    public void deleteQuietly(String publicPath) {

        if (publicPath == null || publicPath.isBlank()) {
            return;
        }

        String fileName =
                publicPath.substring(
                        publicPath.lastIndexOf('/') + 1
                );
