package com.example.csv_splitter.controller;

import com.example.csv_splitter.service.CsvGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/csv")
@RequiredArgsConstructor
@Slf4j
public class CsvController {

    private final CsvGeneratorService csvGeneratorService;

    @PostMapping("/generate")
    public ResponseEntity<?> generateCsvFiles(
            @RequestParam("count") int count,
            @RequestParam(value = "rowsPerFile", required = false) Integer rowsPerFile) {
        try {
            // Determine mode based on parameters
            boolean multiRowMode = rowsPerFile != null && rowsPerFile > 1;

            if (multiRowMode) {
                log.info("Generating {} CSV files with {} rows each (Multi-row mode)", count, rowsPerFile);
            } else {
                log.info("Generating {} CSV files with 1 row each (Normal mode)", count);
            }

            // Validation
            if (count <= 0 || count > 1000) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "success", false,
                                "error", "Count must be between 1 and 1000!"
                        ));
            }

            if (multiRowMode && rowsPerFile <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "success", false,
                                "error", "Rows per file must be at least 1!"
                        ));
            }

            // Call appropriate service method based on mode
            var result = multiRowMode ?
                    csvGeneratorService.generateMultipleRowsFiles(count, rowsPerFile) :
                    csvGeneratorService.generateMultipleCsvFiles(count);

            String successMessage = multiRowMode ?
                    String.format("Generated %d files with %d rows each successfully!", count, rowsPerFile) :
                    String.format("Generated %d files with 1 row each successfully!", count);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", successMessage,
                    "data", result
            ));

        } catch (Exception e) {
            log.error("Error generating files: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "error", e.getMessage(),
                            "details", "Check server logs for more information"
                    ));
        }
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Spring Boot CSV Generator is RUNNING!",
                "instruction", "Use POST /api/csv/generate?count=X&rowsPerFile=Y to generate files"
        ));
    }
}

