package com.finai.service;

import com.finai.ai.AiCategorizationService;
import com.finai.dto.CategorizationResultDTO;
import com.finai.model.Transaction;
import com.finai.model.TransactionCategory;
import com.finai.model.TransactionType;
import com.finai.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Service for CSV Import and Export operations.
 * Conforms to Section 10 of the project specification.
 */
@Service
@Transactional
public class CsvService {

    private static final Logger log = LoggerFactory.getLogger(CsvService.class);

    private final TransactionRepository transactionRepository;
    private final AiCategorizationService aiCategorizationService;

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy")
    );

    public CsvService(TransactionRepository transactionRepository,
                      AiCategorizationService aiCategorizationService) {
        this.transactionRepository = transactionRepository;
        this.aiCategorizationService = aiCategorizationService;
    }

    /**
     * Parse uploaded CSV file, categorize rows via AI/fallback, save to H2 database,
     * and gracefully handle malformed rows.
     */
    public Map<String, Object> importCsv(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded CSV file cannot be empty");
        }

        List<ParsedRow> validRows = new ArrayList<>();
        int malformedRowsCount = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean isHeader = true;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }

                List<String> tokens = parseCsvLine(trimmed);
                if (tokens.isEmpty()) {
                    continue;
                }

                // Detect and skip header row
                if (isHeader) {
                    isHeader = false;
                    String firstToken = tokens.get(0).toLowerCase();
                    if (firstToken.contains("desc") || firstToken.contains("date") ||
                        firstToken.contains("merchant") || firstToken.contains("amount") || firstToken.contains("id")) {
                        continue;
                    }
                }

                ParsedRow row = extractRow(tokens, lineNumber);
                if (row != null) {
                    validRows.add(row);
                } else {
                    malformedRowsCount++;
                    log.warn("Skipping malformed CSV row at line {}: {}", lineNumber, trimmed);
                }
            }
        }

        if (validRows.isEmpty()) {
            throw new IllegalArgumentException("No valid transaction rows found in the uploaded CSV file");
        }

        // Categorize descriptions in batch
        List<String> descriptions = validRows.stream().map(r -> r.description).toList();
        List<CategorizationResultDTO> aiResults = aiCategorizationService.categorizeBatch(descriptions);

        List<Transaction> toSave = new ArrayList<>();
        for (int i = 0; i < validRows.size(); i++) {
            ParsedRow row = validRows.get(i);
            CategorizationResultDTO ai = (i < aiResults.size()) ? aiResults.get(i) : null;

            Transaction tx = new Transaction();
            tx.setRawDescription(row.description);
            tx.setMerchantName((ai != null && ai.getMerchantName() != null) ? ai.getMerchantName() : "Unknown");
            tx.setAmount(row.amount);
            tx.setTransactionDate(row.date);
            tx.setTransactionType(row.type != null ? row.type :
                    (ai != null ? ai.getTransactionType() : TransactionType.DEBIT));
            tx.setCategory((ai != null && ai.getCategory() != null) ? ai.getCategory() : TransactionCategory.OTHER);
            tx.setSubcategory((ai != null && ai.getSubcategory() != null) ? ai.getSubcategory() : "Imported");
            tx.setAiConfidence((ai != null && ai.getConfidence() != null) ? ai.getConfidence() : 0.0);
            tx.setAiExplanation((ai != null && ai.getExplanation() != null) ? ai.getExplanation() : "Imported from CSV");
            tx.setManuallyEdited(false);

            toSave.add(tx);
        }

        List<Transaction> saved = transactionRepository.saveAll(toSave);
        log.info("Successfully imported {} transactions from CSV ({} malformed rows skipped)", saved.size(), malformedRowsCount);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("importedCount", saved.size());
        result.put("skippedCount", malformedRowsCount);
        result.put("totalProcessed", saved.size() + malformedRowsCount);
        return result;
    }

    /**
     * Export all transactions from the H2 database as a structured CSV.
     */
    @Transactional(readOnly = true)
    public byte[] exportCsv() {
        List<Transaction> all = transactionRepository.findAll();
        StringBuilder sb = new StringBuilder();

        // CSV Header
        sb.append("ID,Date,Merchant,Description,Category,Subcategory,Type,Amount,AI Confidence,AI Explanation,Manually Edited\n");

        for (Transaction tx : all) {
            sb.append(tx.getId()).append(",");
            sb.append(tx.getTransactionDate()).append(",");
            sb.append(escapeCsvField(tx.getMerchantName())).append(",");
            sb.append(escapeCsvField(tx.getRawDescription())).append(",");
            sb.append(escapeCsvField(tx.getCategory() != null ? tx.getCategory().name() : "OTHER")).append(",");
            sb.append(escapeCsvField(tx.getSubcategory())).append(",");
            sb.append(tx.getTransactionType()).append(",");
            sb.append(tx.getAmount()).append(",");
            sb.append(String.format(Locale.US, "%.2f", tx.getAiConfidence() != null ? tx.getAiConfidence() : 0.0)).append(",");
            sb.append(escapeCsvField(tx.getAiExplanation())).append(",");
            sb.append(Boolean.TRUE.equals(tx.getManuallyEdited()) ? "Yes" : "No");
            sb.append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private ParsedRow extractRow(List<String> tokens, int lineNumber) {
        if (tokens.isEmpty()) return null;

        String description;
        BigDecimal amount = BigDecimal.ZERO;
        LocalDate date = LocalDate.now();
        TransactionType type = TransactionType.DEBIT;

        if (tokens.size() == 1) {
            // Single column: raw description only
            description = tokens.get(0).trim();
            if (description.isEmpty()) return null;
        } else if (tokens.size() == 2) {
            // Description, Amount
            description = tokens.get(0).trim();
            amount = parseAmount(tokens.get(1));
        } else if (tokens.size() >= 3) {
            // Could be: Date, Description, Amount OR Description, Amount, Date
            String col0 = tokens.get(0).trim();
            String col1 = tokens.get(1).trim();
            String col2 = tokens.get(2).trim();

            LocalDate parsedDate0 = parseDate(col0);
            if (parsedDate0 != null) {
                // Format: Date, Description, Amount
                date = parsedDate0;
                description = col1;
                amount = parseAmount(col2);
                if (tokens.size() >= 4) {
                    type = parseType(tokens.get(3));
                }
            } else {
                // Format: Description, Amount, Date
                description = col0;
                amount = parseAmount(col1);
                LocalDate parsedDate2 = parseDate(col2);
                if (parsedDate2 != null) date = parsedDate2;
                if (tokens.size() >= 4) {
                    type = parseType(tokens.get(3));
                }
            }
        } else {
            return null;
        }

        if (description.isBlank()) return null;
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            amount = amount.abs();
            type = TransactionType.DEBIT;
        }

        return new ParsedRow(description, amount, date, type);
    }

    private BigDecimal parseAmount(String raw) {
        if (raw == null) return BigDecimal.ZERO;
        String clean = raw.replaceAll("[^0-9.-]", "").trim();
        if (clean.isEmpty() || clean.equals("-") || clean.equals(".")) return BigDecimal.ZERO;
        try {
            return new BigDecimal(clean);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String clean = raw.trim();
        for (DateTimeFormatter fmt : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(clean, fmt);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    private TransactionType parseType(String raw) {
        if (raw == null) return TransactionType.DEBIT;
        String upper = raw.toUpperCase().trim();
        if (upper.contains("CREDIT") || upper.equals("CR")) {
            return TransactionType.CREDIT;
        }
        return TransactionType.DEBIT;
    }

    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(cur.toString().trim());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        result.add(cur.toString().trim());
        return result;
    }

    private String escapeCsvField(String field) {
        if (field == null) return "\"\"";
        String clean = field.replace("\"", "\"\"");
        if (clean.contains(",") || clean.contains("\n") || clean.contains("\"")) {
            return "\"" + clean + "\"";
        }
        return "\"" + clean + "\"";
    }

    private record ParsedRow(String description, BigDecimal amount, LocalDate date, TransactionType type) {}
}
