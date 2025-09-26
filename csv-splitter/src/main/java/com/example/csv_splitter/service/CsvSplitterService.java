package com.example.csv_splitter.service;


import com.example.csv_splitter.dto.SplitResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CsvSplitterService {

    private static final String OUTPUT_DIR = "split_files";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public SplitResult splitCsvIntoSeparateFiles(MultipartFile file) throws Exception {
        log.info("Starting CSV split process for file: {}", file.getOriginalFilename());

        // Create output directory
        Path outputPath = Paths.get(OUTPUT_DIR);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
            log.info("Created output directory: {}", OUTPUT_DIR);
        }

        List<String> generatedFiles = new ArrayList<>();
        String timestamp = LocalDateTime.now().format(formatter);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                String fileName = String.format("%s/line_%d_%s.csv", OUTPUT_DIR, lineNumber, timestamp);

                try (FileWriter writer = new FileWriter(fileName)) {
                    writer.write(line);
                    writer.write(System.lineSeparator()); // Add newline for proper CSV format
                    generatedFiles.add(fileName);
                    log.info("Created file: {} with content: {}", fileName, line.substring(0, Math.min(line.length(), 50)) + "...");
                }
                lineNumber++;
            }
        }

        log.info("Successfully split CSV into {} files", generatedFiles.size());
        return SplitResult.builder()
                .totalLinesProcessed(generatedFiles.size())
                .generatedFiles(generatedFiles)
                .outputDirectory(OUTPUT_DIR)
                .timestamp(timestamp)
                .originalFileName(file.getOriginalFilename())
                .build();
    }
}

