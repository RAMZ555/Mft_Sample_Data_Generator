package com.example.csv_splitter.service;

import com.example.csv_splitter.dto.GenerateResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CsvGeneratorService {

    private static final String OUTPUT_DIR = "generated_files";
    private static final DateTimeFormatter timestampFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    // Hardcoded template data
    private static final String TEMPLATE = "31024000,,Template001,F15-796-514200,Internal Transfer,INR,{POSITION_8},+{POSITION_9},Cust_Ref_0001,1,,,,,,,,,,,,,,,,,,,SG123456789012345678,,Beneficiary_1,Beneficiary_2,Townsville,Bank Branch,,,,,,,SBIN0000001,,,,,,State Bank,,,,,,,,,,,,2,,,,,,,,,,,H2H UFF Test file,,,,,,,abc@gmail.com,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,Individual Debit,Y,Invoice Date|Invoice No|Description|Amount~20230801|INV001|Goods|1000.00";

    public GenerateResult generateMultipleCsvFiles(int count) throws Exception {
        log.info("Starting CSV generation process for {} files", count);

        // Create output directory
        Path outputPath = Paths.get(OUTPUT_DIR);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
            log.info("Created output directory: {}", OUTPUT_DIR);
        }

        // Clean up any existing files first (optional)
        cleanupExistingFiles(outputPath);

        List<String> generatedFiles = new ArrayList<>();
        String timestamp = LocalDateTime.now().format(timestampFormatter);
        String tomorrowDate = LocalDate.now().plusDays(1).format(dateFormatter);

        for (int i = 1; i <= count; i++) {
            // Position 8: Start from 2000, increment by 1
            int position8Value = 2000 + (i - 1);

            // Replace placeholders in template
            String csvContent = TEMPLATE
                    .replace("{POSITION_8}", String.valueOf(position8Value))
                    .replace("{POSITION_9}", tomorrowDate);

            String fileName = String.format("%s/file_%03d.csv", OUTPUT_DIR, i);
            Path filePath = Paths.get(fileName);

            try {
                // Method 1: Using Files.write (recommended)
                Files.write(filePath, (csvContent + System.lineSeparator()).getBytes());
                generatedFiles.add(fileName);

                if (i <= 3 || i == count) { // Log first 3 and last file
                    log.info("Generated file {}: {} with position8={}, date={}", i, fileName, position8Value, tomorrowDate);
                }

                // Small delay to prevent rapid file operations (optional)
                if (i % 10 == 0) {
                    Thread.sleep(1);
                }

            } catch (IOException e) {
                log.error("Failed to create file {}: {}", fileName, e.getMessage());
                // Try alternative method with BufferedWriter
                try {
                    createFileWithBufferedWriter(fileName, csvContent);
                    generatedFiles.add(fileName);
                    log.info("Successfully created file {} using BufferedWriter", fileName);
                } catch (IOException e2) {
                    log.error("Failed to create file {} with BufferedWriter: {}", fileName, e2.getMessage());
                    throw new RuntimeException("Unable to create file: " + fileName, e2);
                }
            }
        }

        log.info("Successfully generated {} CSV files", generatedFiles.size());
        return GenerateResult.builder()
                .totalFilesGenerated(generatedFiles.size())
                .generatedFiles(generatedFiles)
                .outputDirectory(OUTPUT_DIR)
                .timestamp(timestamp)
                .tomorrowDate(tomorrowDate)
                .startingPosition8Value(2000)
                .endingPosition8Value(2000 + count - 1)
                .build();
    }

    private void createFileWithBufferedWriter(String fileName, String content) throws IOException {
        // Use try-with-resources to ensure proper resource cleanup
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName))) {
            writer.write(content);
            writer.newLine();
        }
    }

    private void cleanupExistingFiles(Path outputPath) {
        try {
            if (Files.exists(outputPath)) {
                Files.list(outputPath)
                        .filter(path -> path.toString().endsWith(".csv"))
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException e) {
                                log.warn("Could not delete existing file: {}", path);
                            }
                        });
                log.info("Cleaned up existing CSV files");
            }
        } catch (IOException e) {
            log.warn("Could not clean up existing files: {}", e.getMessage());
        }
    }

    // Alternative method using NIO with explicit file creation
    public GenerateResult generateMultipleCsvFilesNIO(int count) throws Exception {
        log.info("Starting CSV generation process for {} files using NIO", count);

        Path outputPath = Paths.get(OUTPUT_DIR);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }

        cleanupExistingFiles(outputPath);

        List<String> generatedFiles = new ArrayList<>();
        String timestamp = LocalDateTime.now().format(timestampFormatter);
        String tomorrowDate = LocalDate.now().plusDays(1).format(dateFormatter);

        for (int i = 1; i <= count; i++) {
            int position8Value = 2000 + (i - 1);
            String csvContent = TEMPLATE
                    .replace("{POSITION_8}", String.valueOf(position8Value))
                    .replace("{POSITION_9}", tomorrowDate);

            String fileName = String.format("file_%03d.csv", i);
            Path filePath = outputPath.resolve(fileName);

            // Ensure file doesn't exist
            Files.deleteIfExists(filePath);

            // Create file with content
            Files.write(filePath, (csvContent + System.lineSeparator()).getBytes());
            generatedFiles.add(filePath.toString());

            if (i <= 3 || i == count) {
                log.info("Generated file {}: {} with position8={}, date={}",
                        i, filePath.toString(), position8Value, tomorrowDate);
            }
        }

        log.info("Successfully generated {} CSV files using NIO", generatedFiles.size());
        return GenerateResult.builder()
                .totalFilesGenerated(generatedFiles.size())
                .generatedFiles(generatedFiles)
                .outputDirectory(OUTPUT_DIR)
                .timestamp(timestamp)
                .tomorrowDate(tomorrowDate)
                .startingPosition8Value(2000)
                .endingPosition8Value(2000 + count - 1)
                .build();
    }


    public GenerateResult generateMultipleRowsFiles(int fileCount, int rowsPerFile) throws Exception {
        log.info("Starting CSV generation: {} files with {} rows each", fileCount, rowsPerFile);

        // Create output directory
        Path outputPath = Paths.get(OUTPUT_DIR);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
            log.info("Created output directory: {}", OUTPUT_DIR);
        }

        // Clean up any existing files first
        cleanupExistingFiles(outputPath);

        List<String> generatedFiles = new ArrayList<>();
        String timestamp = LocalDateTime.now().format(timestampFormatter);
        String tomorrowDate = LocalDate.now().plusDays(1).format(dateFormatter);
        int currentPosition8 = 2000;

        for (int fileIndex = 1; fileIndex <= fileCount; fileIndex++) {
            String fileName = String.format("%s/file_%03d.csv", OUTPUT_DIR, fileIndex);
            Path filePath = Paths.get(fileName);

            // Build content for this file with multiple rows
            StringBuilder fileContent = new StringBuilder();

            for (int rowIndex = 1; rowIndex <= rowsPerFile; rowIndex++) {
                String csvRow = TEMPLATE
                        .replace("{POSITION_8}", String.valueOf(currentPosition8))
                        .replace("{POSITION_9}", tomorrowDate);

                fileContent.append(csvRow).append(System.lineSeparator());
                currentPosition8++; // Increment for each row across all files
            }

            try {
                // Write file with multiple rows
                Files.write(filePath, fileContent.toString().getBytes());
                generatedFiles.add(fileName);

                if (fileIndex <= 3 || fileIndex == fileCount) { // Log first 3 and last file
                    log.info("Generated file {}: {} with {} rows, position8 range: {} to {}",
                            fileIndex, fileName, rowsPerFile,
                            (currentPosition8 - rowsPerFile), (currentPosition8 - 1));
                }

                // Small delay to prevent rapid file operations
                if (fileIndex % 10 == 0) {
                    Thread.sleep(1);
                }

            } catch (IOException e) {
                log.error("Failed to create file {}: {}", fileName, e.getMessage());
                throw new RuntimeException("Unable to create file: " + fileName, e);
            }
        }

        int totalRows = fileCount * rowsPerFile;
        log.info("Successfully generated {} CSV files with {} rows each (total: {} rows)",
                fileCount, rowsPerFile, totalRows);

        return GenerateResult.builder()
                .totalFilesGenerated(generatedFiles.size())
                .generatedFiles(generatedFiles)
                .outputDirectory(OUTPUT_DIR)
                .timestamp(timestamp)
                .tomorrowDate(tomorrowDate)
                .startingPosition8Value(2000)
                .endingPosition8Value(currentPosition8 - 1)
                .rowsPerFile(rowsPerFile)
                .totalRows(totalRows)
                .build();
    }
}
