package com.finai.ai;

import com.finai.dto.CategorizationResultDTO;
import com.finai.model.TransactionCategory;
import com.finai.model.TransactionType;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Intelligent rule-based and pattern-matching fallback categorizer.
 * Ensures 100% application uptime when cloud AI is unreachable or API key is not configured.
 * Conforms to Section 3 and Section 4 of the project specification.
 */
@Service
public class RuleBasedFallbackService {

    public static final String PROVIDER_NAME = "Rule-Based Heuristic Engine (Fallback Mode)";

    private static final List<RuleDefinition> RULES = new ArrayList<>();

    static {
        // High priority: Salary & Rent
        addRule(TransactionCategory.TRANSFERS, "Salary & Employment",
                List.of("SALARY", "PAYROLL", "STIPEND", "BONUS"), 0.99,
                "Direct employment payroll or compensation credit.", TransactionType.CREDIT);
        addRule(TransactionCategory.RENT, "Residential Rent",
                List.of("RENT", "LEASE", "LANDLORD", "APARTMENT RENT", "HOUSE RENT", "NOBROKER RENT"), 0.99,
                "Residential apartment rental or property lease.");

        // 1. Food
        addRule(TransactionCategory.FOOD, "Food Delivery",
                List.of("SWIGGY", "ZOMATO", "BLINKIT", "ZEPTO", "INSTAMART", "EATS"), 0.98,
                "Matched well-known on-demand food delivery service.");
        addRule(TransactionCategory.FOOD, "Coffee & Cafes",
                List.of("STARBUCKS", "CAFE COFFEE DAY", "THIRD WAVE", "BLUE TOKAI", "CHAI POINT", "CHAAYOS"), 0.97,
                "Matched specialty cafe or coffee house.");
        addRule(TransactionCategory.FOOD, "Fast Food & Dining",
                List.of("MCDONALD", "KFC", "DOMINO", "PIZZA", "BURGER KING", "SUBWAY", "HALDIRAM", "BARBEQUE NATION", "RESTAURANT", "CAFE", "BAKERY", "DHABA"), 0.95,
                "Identified restaurant or dining establishment.");
        addRule(TransactionCategory.FOOD, "Groceries",
                List.of("BIGBASKET", "NATURE'S BASKET", "NATURE BASKET", "DMART", "SPENCER", "MORE RETAIL", "GROCERY", "SUPERMARKET", "VEGETABLE", "FRUITS"), 0.95,
                "Identified supermarket or grocery retailer.");

        // 2. Travel
        addRule(TransactionCategory.TRAVEL, "Rideshare & Cabs",
                List.of("UBER", "OLA CABS", "OLA", "RAPIDO", "BLUSMART"), 0.98,
                "Matched urban transit or ridesharing service.");
        addRule(TransactionCategory.TRAVEL, "Rail & Transit",
                List.of("IRCTC", "INDIAN RAILWAY", "METRO", "RAILWAY", "DELHI METRO", "BMRCL"), 0.99,
                "Recognized railway or urban metro transit provider.");
        addRule(TransactionCategory.TRAVEL, "Flights & Travel Booking",
                List.of("MAKEMYTRIP", "CLEARTRIP", "INDIGO", "AIR INDIA", "SPICEJET", "VISTARA", "AKASA", "YATRA", "EXPEDIA", "FLIGHT"), 0.96,
                "Identified airline or travel reservation agency.");
        addRule(TransactionCategory.TRAVEL, "Fuel & Toll",
                List.of("PETROL", "DIESEL", "HPCL", "BPCL", "IOCL", "SHELL", "INDIAN OIL", "FASTAG", "TOLL", "FUEL"), 0.95,
                "Fuel filling station or highway electronic toll payment.");

        // 3. Utilities
        addRule(TransactionCategory.UTILITIES, "Electricity",
                List.of("BESCOM", "TATA POWER", "ADANI ELECTRICITY", "MSEDCL", "TORRENT POWER", "ELECTRICITY", "POWER BILL"), 0.99,
                "Recognized municipal or regional electricity provider.");
        addRule(TransactionCategory.UTILITIES, "Telecom & Broadband",
                List.of("AIRTEL", "JIO", "VODAFONE", "VI", "ACT FIBERNET", "HATHWAY", "BROADBAND", "MOBILE RECHARGE", "BSNL"), 0.97,
                "Telecommunications, cellular, or broadband internet payment.");
        addRule(TransactionCategory.UTILITIES, "Gas & Water",
                List.of("BWSSB", "WATER BILL", "IGL", "MAHANAGAR GAS", "INDANE", "BHARAT GAS", "HP GAS", "PIPED GAS", "CYLINDER"), 0.96,
                "Municipal water or domestic cooking gas utility.");

        // 4. Shopping
        addRule(TransactionCategory.SHOPPING, "E-Commerce",
                List.of("AMAZON", "FLIPKART", "MYNTRA", "AJIO", "NYKAA", "MEESHO", "TATA CLiQ"), 0.97,
                "Major e-commerce and online retail platform.");
        addRule(TransactionCategory.SHOPPING, "Apparel & Retail",
                List.of("ZARA", "H&M", "LEVIS", "MARKS & SPENCER", "WESTSIDE", "PANTALOONS", "DECATHLON", "NIKE", "ADIDAS", "RETAIL", "CLOTHING"), 0.95,
                "Apparel, sportswear, or department store retail purchase.");
        addRule(TransactionCategory.SHOPPING, "Electronics",
                List.of("CROMA", "RELIANCE DIGITAL", "VIJAY SALES", "APPLE STORE", "SAMSUNG", "ELECTRONICS"), 0.95,
                "Consumer electronics or computer hardware retailer.");

        // 5. Entertainment
        addRule(TransactionCategory.ENTERTAINMENT, "Streaming Services",
                List.of("NETFLIX", "SPOTIFY", "PRIME VIDEO", "DISNEY", "HOTSTAR", "APPLE MUSIC", "SONYLIV", "ZEE5", "YOUTUBE PREMIUM"), 0.99,
                "Digital audio/video streaming media subscription.");
        addRule(TransactionCategory.ENTERTAINMENT, "Movies & Cinema",
                List.of("BOOKMYSHOW", "PVR", "INOX", "CINEPOLIS", "CINEMA", "MOVIE"), 0.98,
                "Theatrical cinema or entertainment ticketing.");
        addRule(TransactionCategory.ENTERTAINMENT, "Gaming & Events",
                List.of("STEAM", "PLAYSTATION", "XBOX", "NINTENDO", "GAMING", "CONCERT", "EVENT TICKETS"), 0.95,
                "Video game digital purchase or live event entertainment.");

        // 6. Healthcare
        addRule(TransactionCategory.HEALTHCARE, "Pharmacy & Medicines",
                List.of("APOLLO PHARMACY", "MEDPLUS", "PHARMEASY", "1MG", "NETMEDS", "PHARMACY", "CHEMIST", "MEDICINE"), 0.98,
                "Pharmaceutical products and prescription medications.");
        addRule(TransactionCategory.HEALTHCARE, "Medical Consultation & Clinics",
                List.of("PRACTO", "HOSPITAL", "CLINIC", "DOCTOR", "DIAGNOSTICS", "LAL PATHLABS", "METROPOLIS", "HEALTHCARE"), 0.96,
                "Clinical consultation, diagnostic lab, or medical center.");

        // 7. Education
        addRule(TransactionCategory.EDUCATION, "Online Learning",
                List.of("UDEMY", "COURSERA", "EDX", "UNACADEMY", "BYJU", "PHYSICSWALLAH", "SKILLSHARE", "LINKEDIN LEARNING"), 0.98,
                "Online education course or certification platform.");
        addRule(TransactionCategory.EDUCATION, "Academic & Books",
                List.of("COLLEGE", "UNIVERSITY", "SCHOOL", "TUITION", "BOOKSTORE", "OXFORD BOOK", "TEXTBOOK", "ACADEMIC"), 0.95,
                "Academic institution tuition or educational literature.");

        // 8. Rent
        addRule(TransactionCategory.RENT, "Residential Rent",
                List.of("RENT", "LEASE", "LANDLORD", "APARTMENT RENT", "HOUSE RENT", "NOBROKER RENT"), 0.99,
                "Residential apartment rental or property lease.");
        addRule(TransactionCategory.RENT, "Society Maintenance",
                List.of("SOCIETY MAINTENANCE", "APARTMENT MAINTENANCE", "MAINTENANCE CHARGES", "MYGATE", "APNACOMPLEX"), 0.95,
                "Residential society maintenance fee.");

        // 9. Transfers & Income
        addRule(TransactionCategory.TRANSFERS, "Salary & Employment",
                List.of("SALARY", "PAYROLL", "STIPEND", "BONUS"), 0.99,
                "Direct employment payroll or compensation credit.", TransactionType.CREDIT);
        addRule(TransactionCategory.TRANSFERS, "Investments & Interest",
                List.of("DIVIDEND", "INTEREST CREDIT", "SAVINGS INTEREST", "MUTUAL FUND", "ZERODHA", "GROWW"), 0.96,
                "Investment platform distribution or interest income.");
        addRule(TransactionCategory.TRANSFERS, "Peer-to-Peer & Transfers",
                List.of("P2P", "TRANSFER TO", "SENT TO", "RECEIVED FROM", "IMPS", "NEFT", "RTGS", "PAYTM TRANSFER"), 0.90,
                "Direct peer-to-peer or inter-bank account transfer.");
        addRule(TransactionCategory.TRANSFERS, "Cashback & Rewards",
                List.of("CASHBACK", "REWARD", "CRED CASHBACK", "PROMO BONUS"), 0.94,
                "Promotional cashback or rewards deposit.", TransactionType.CREDIT);
    }

    /**
     * Categorize a single transaction description using keyword patterns.
     */
    public CategorizationResultDTO categorize(String rawDescription) {
        if (rawDescription == null || rawDescription.trim().isEmpty()) {
            return new CategorizationResultDTO("", "Unknown",
                    TransactionCategory.OTHER, "Unspecified",
                    TransactionType.DEBIT, 0.0,
                    "Empty transaction description provided.",
                    PROVIDER_NAME, true);
        }

        String normalized = rawDescription.toUpperCase(Locale.ENGLISH).trim();
        String extractedMerchant = extractMerchantName(rawDescription);

        // Check rules in order of priority
        for (RuleDefinition rule : RULES) {
            for (String keyword : rule.keywords) {
                if (containsKeyword(normalized, keyword)) {
                    TransactionType type = rule.preferredType != null ? rule.preferredType : detectTransactionType(normalized);
                    return new CategorizationResultDTO(
                            rawDescription,
                            extractedMerchant.equalsIgnoreCase("Unknown") ? formatMerchant(keyword) : extractedMerchant,
                            rule.category,
                            rule.subcategory,
                            type,
                            rule.confidence,
                            rule.explanation + " (Pattern matched: '" + keyword + "')",
                            PROVIDER_NAME,
                            true
                    );
                }
            }
        }

        // Fallback for Ambiguous / Unknown transactions
        TransactionType type = detectTransactionType(normalized);
        return new CategorizationResultDTO(
                rawDescription,
                extractedMerchant,
                TransactionCategory.OTHER,
                "General Transaction",
                type,
                0.35,
                "Transaction description contains no recognizable merchant or purpose keywords. Categorized as Other.",
                PROVIDER_NAME,
                true
        );
    }

    /**
     * Categorize multiple transactions.
     */
    public List<CategorizationResultDTO> categorizeBatch(List<String> descriptions) {
        List<CategorizationResultDTO> results = new ArrayList<>();
        if (descriptions != null) {
            for (String desc : descriptions) {
                results.add(categorize(desc));
            }
        }
        return results;
    }

    private boolean containsKeyword(String text, String keyword) {
        String escaped = Pattern.quote(keyword);
        Pattern p = Pattern.compile("(?i)(^|[^A-Z0-9])" + escaped + "([^A-Z0-9]|$)");
        if (p.matcher(text).find()) {
            return true;
        }
        // Substring match only for longer distinct words (>= 5 chars)
        return keyword.length() >= 5 && text.contains(keyword);
    }

    private String extractMerchantName(String raw) {
        String clean = raw.trim();

        // 1. UPI patterns: UPI/<id>/<MERCHANT> [LOCATION]
        Matcher upiMatcher = Pattern.compile("(?i)UPI/\\w+/([A-Za-z0-9\\s&'-]+)").matcher(clean);
        if (upiMatcher.find()) {
            String candidate = upiMatcher.group(1).trim();
            return cleanTrailingLocation(candidate);
        }

        // 2. POS patterns: POS-<id>/<MERCHANT> or POS/<MERCHANT>
        Matcher posMatcher = Pattern.compile("(?i)POS[/-]\\w*[/-]?([A-Za-z0-9\\s&'-]+)").matcher(clean);
        if (posMatcher.find()) {
            String candidate = posMatcher.group(1).trim();
            return cleanTrailingLocation(candidate);
        }

        // 3. Slash delimited: AMAZON PAY/ORDER/82918 -> Amazon Pay
        if (clean.contains("/")) {
            String[] segments = clean.split("/");
            for (String seg : segments) {
                String trimmed = seg.trim();
                if (trimmed.length() >= 3 && !trimmed.matches(".*\\d{5,}.*")) {
                    return cleanTrailingLocation(trimmed);
                }
            }
        }

        // 4. Default: first 2-3 words if reasonable
        String[] words = clean.split("\\s+");
        if (words.length > 0 && words[0].length() > 2) {
            if (words.length >= 2 && words[1].length() > 2 && !words[1].matches(".*\\d.*")) {
                return formatMerchant(words[0] + " " + words[1]);
            }
            return formatMerchant(words[0]);
        }

        return "Unknown";
    }

    private String cleanTrailingLocation(String merchant) {
        // Strip common Indian cities or store numbers often appended in bank text
        String stripped = merchant.replaceAll("(?i)\\b(BANGALORE|BLR|MUMBAI|DELHI|NEW DELHI|HYDERABAD|CHENNAI|PUNE|KORAMANGALA|WHITEFIELD|INDIRANAGAR)\\b.*$", "").trim();
        stripped = stripped.replaceAll("\\b(PVT LTD|LIMITED|LTD|INDIA)\\b.*$", "").trim();
        if (stripped.isBlank()) {
            return formatMerchant(merchant);
        }
        return formatMerchant(stripped);
    }

    private String formatMerchant(String raw) {
        if (raw == null || raw.isBlank()) return "Unknown";
        String[] words = raw.toLowerCase(Locale.ENGLISH).split("[\\s_]+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    private TransactionType detectTransactionType(String text) {
        if (text.contains("CREDIT") || text.contains("SALARY") || text.contains("CASHBACK") ||
            text.contains("DIVIDEND") || text.contains("REFUND") || text.contains("INTEREST")) {
            return TransactionType.CREDIT;
        }
        return TransactionType.DEBIT;
    }

    private static void addRule(TransactionCategory cat, String subcat, List<String> kw, double conf, String exp) {
        RULES.add(new RuleDefinition(cat, subcat, kw, conf, exp, null));
    }

    private static void addRule(TransactionCategory cat, String subcat, List<String> kw, double conf, String exp, TransactionType prefType) {
        RULES.add(new RuleDefinition(cat, subcat, kw, conf, exp, prefType));
    }

    private record RuleDefinition(
            TransactionCategory category,
            String subcategory,
            List<String> keywords,
            double confidence,
            String explanation,
            TransactionType preferredType
    ) {}
}
