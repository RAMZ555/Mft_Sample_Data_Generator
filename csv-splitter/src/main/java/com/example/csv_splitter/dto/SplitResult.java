package com.example.csv_splitter.dto;


import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SplitResult {
    private String originalFileName;
    private int totalLinesProcessed;
    private List<String> generatedFiles;
    private String outputDirectory;
    private String timestamp;
}
