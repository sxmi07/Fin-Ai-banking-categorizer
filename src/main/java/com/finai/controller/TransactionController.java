package com.finai.controller;

import com.finai.dto.*;
import com.finai.model.TransactionCategory;
import com.finai.model.TransactionType;
import com.finai.service.CsvService;
import com.finai.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Transaction operations.
 * Implements endpoints defined in Section 8 of the project specification.
 */
@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionService transactionService;
    private final CsvService csvService;

    public TransactionController(TransactionService transactionService, CsvService csvService) {
        this.transactionService = transactionService;
        this.csvService = csvService;
    }

    /**
     * POST /api/transactions/import
     * Parse uploaded CSV statement, categorize rows via AI/fallback, and save to H2.
     */
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> importCsv(
            @RequestParam("file") MultipartFile file) throws IOException {

        Map<String, Object> summary = csvService.importCsv(file);
        return ResponseEntity.ok(ApiResponse.success("CSV statement imported and categorized successfully", summary));
    }

    /**
     * GET /api/transactions/export
     * Download all categorized transactions from H2 as a CSV file.
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv() {
        byte[] csvData = csvService.exportCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"finai_transactions.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData);
    }

    /**
     * POST /api/transactions/categorize
     * Categorize transaction descriptions using the AI engine (with fallback).
     */
    @PostMapping("/categorize")
    public ResponseEntity<ApiResponse<List<CategorizationResultDTO>>> categorize(
            @Valid @RequestBody CategorizeRequestDTO request) {

        List<CategorizationResultDTO> results = transactionService.categorizeTransactions(request);
        return ResponseEntity.ok(ApiResponse.success("Transactions categorized successfully", results));
    }

    /**
     * GET /api/transactions
     * Retrieve paginated and filtered transactions.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<TransactionResponseDTO>>> getTransactions(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);

        TransactionCategory catEnum = (category != null && !category.isBlank()) ? TransactionCategory.fromString(category) : null;
        TransactionType typeEnum = (type != null && !type.isBlank()) ? TransactionType.fromString(type) : null;

        Page<TransactionResponseDTO> result = transactionService.getTransactions(search, catEnum, typeEnum, startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * GET /api/transactions/summary
     * Retrieve financial summary and analytics.
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<TransactionSummaryDTO>> getSummary(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        TransactionSummaryDTO summary = transactionService.getSummary(year, month);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    /**
     * GET /api/transactions/{id}
     * Retrieve a single transaction by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponseDTO>> getTransactionById(@PathVariable Long id) {
        TransactionResponseDTO tx = transactionService.getTransactionById(id);
        return ResponseEntity.ok(ApiResponse.success(tx));
    }

    /**
     * POST /api/transactions
     * Create a new transaction manually.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponseDTO>> createTransaction(
            @Valid @RequestBody TransactionRequestDTO request) {

        TransactionResponseDTO created = transactionService.createTransaction(request);
        return new ResponseEntity<>(ApiResponse.success("Transaction created successfully", created), HttpStatus.CREATED);
    }

    /**
     * PUT /api/transactions/{id}/category
     * Manually update a transaction category (Section 11).
     */
    @PutMapping("/{id}/category")
    public ResponseEntity<ApiResponse<TransactionResponseDTO>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequestDTO request) {

        TransactionResponseDTO updated = transactionService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", updated));
    }

    /**
     * DELETE /api/transactions/{id}
     * Delete a transaction by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.ok(ApiResponse.success("Transaction deleted successfully", null));
    }
}
