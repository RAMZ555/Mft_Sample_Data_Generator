package com.example.csv_splitter.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class GenerateResult {
    private int totalFilesGenerated;
    private List<String> generatedFiles;
    private String outputDirectory;
    private String timestamp;
    private String tomorrowDate;
    private int startingPosition8Value;
    private int endingPosition8Value;

    // New fields for multiple rows support
    private Integer rowsPerFile;  // Using Integer so it can be null for single-row files
    private Integer totalRows;    // Using Integer so it can be null for single-row files
    private String fileName;      // For single file generation
}