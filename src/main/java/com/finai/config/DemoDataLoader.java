package com.finai.config;

import com.finai.model.Transaction;
import com.finai.model.TransactionCategory;
import com.finai.model.TransactionType;
import com.finai.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Initializes fictional sample demonstration data if database is empty.
 * Conforms to Section 12 of the project specification (at least 25 sample transactions).
 * ALL DATA IN THIS FILE IS FICTIONAL FOR DEMONSTRATION PURPOSES ONLY.
 */
@Component
public class DemoDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataLoader.class);

    private final TransactionRepository transactionRepository;

    public DemoDataLoader(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void run(String... args) {
        if (transactionRepository.count() > 0) {
            log.info("Database already contains transactions. Skipping demo data initialization.");
            return;
        }
        reloadDemoData();
    }

    /**
     * Resets database and reloads the 30 fictional sample transactions.
     */
    public int reloadDemoData() {
        log.info("Populating database with 30 fictional demonstration banking transactions...");
        transactionRepository.deleteAll();

        List<Transaction> samples = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // 1. Food
        samples.add(createSample("UPI/409283/SWIGGY BANGALORE", "Swiggy",
                new BigDecimal("420.50"), today.minusDays(1), TransactionType.DEBIT,
                TransactionCategory.FOOD, "Food Delivery", 0.98,
                "Identified popular food delivery service 'Swiggy' via UPI reference."));

        samples.add(createSample("POS-9921/ZOMATO RESTAURANT ORDER", "Zomato",
                new BigDecimal("680.00"), today.minusDays(2), TransactionType.DEBIT,
                TransactionCategory.FOOD, "Dining Out", 0.97,
                "Matched 'Zomato' descriptor indicating food purchase."));

        samples.add(createSample("STARBUCKS COFFEE KORAMANGALA", "Starbucks",
                new BigDecimal("350.00"), today.minusDays(4), TransactionType.DEBIT,
                TransactionCategory.FOOD, "Coffee & Cafes", 0.99,
                "Merchant 'Starbucks' classified under food & beverages."));

        samples.add(createSample("NATURE BASKET SUPERMARKET GROCERIES", "Nature's Basket",
                new BigDecimal("1850.75"), today.minusDays(7), TransactionType.DEBIT,
                TransactionCategory.FOOD, "Groceries", 0.95,
                "Identified retail grocery merchant 'Nature Basket'."));

        // 2. Travel
        samples.add(createSample("UPI/829182/UBER INDIA MUMBAI", "Uber",
                new BigDecimal("310.00"), today.minusDays(1), TransactionType.DEBIT,
                TransactionCategory.TRAVEL, "Rideshare", 0.98,
                "Identified urban ridesharing service 'Uber'."));

        samples.add(createSample("IRCTC TICKET BOOKING NEW DELHI", "IRCTC",
                new BigDecimal("1420.00"), today.minusDays(5), TransactionType.DEBIT,
                TransactionCategory.TRAVEL, "Train Transit", 0.99,
                "Indian Railway Catering and Tourism Corporation booking."));

        samples.add(createSample("MAKEMYTRIP FLIGHT BLR-DEL", "MakeMyTrip",
                new BigDecimal("5490.00"), today.minusDays(12), TransactionType.DEBIT,
                TransactionCategory.TRAVEL, "Flights", 0.96,
                "Travel booking platform 'MakeMyTrip' flight purchase."));

        samples.add(createSample("HPCL PETROL PUMP WHITEFIELD", "HPCL",
                new BigDecimal("2100.00"), today.minusDays(6), TransactionType.DEBIT,
                TransactionCategory.TRAVEL, "Fuel", 0.94,
                "Fuel filling station transaction."));

        // 3. Utilities
        samples.add(createSample("BESCOM ELECTRICITY BILL BANGALORE", "BESCOM",
                new BigDecimal("1240.00"), today.minusDays(3), TransactionType.DEBIT,
                TransactionCategory.UTILITIES, "Electricity", 0.99,
                "Bangalore Electricity Supply Company power utility."));

        samples.add(createSample("AIRTEL FIBER BROADBAND RECHARGE", "Airtel",
                new BigDecimal("999.00"), today.minusDays(8), TransactionType.DEBIT,
                TransactionCategory.UTILITIES, "Internet", 0.98,
                "Telecommunication broadband and fiber provider."));

        samples.add(createSample("JIO PREPAID MOB RECHARGE 3M", "Jio",
                new BigDecimal("749.00"), today.minusDays(14), TransactionType.DEBIT,
                TransactionCategory.UTILITIES, "Mobile", 0.97,
                "Cellular service mobile recharge."));

        // 4. Shopping
        samples.add(createSample("AMAZON PAY/ORDER/82918 ELECTRONICS", "Amazon",
                new BigDecimal("3299.00"), today.minusDays(2), TransactionType.DEBIT,
                TransactionCategory.SHOPPING, "Electronics", 0.97,
                "E-commerce purchase on Amazon marketplace."));

        samples.add(createSample("FLIPKART INTERNET PVT LTD CLOTHING", "Flipkart",
                new BigDecimal("1499.00"), today.minusDays(9), TransactionType.DEBIT,
                TransactionCategory.SHOPPING, "Apparel", 0.96,
                "Online retail shopping on Flipkart."));

        samples.add(createSample("ZARA RETAIL FORUM MALL", "Zara",
                new BigDecimal("4590.00"), today.minusDays(15), TransactionType.DEBIT,
                TransactionCategory.SHOPPING, "Clothing", 0.98,
                "Fashion and apparel retail purchase."));

        // 5. Entertainment
        samples.add(createSample("NETFLIX SUBSCRIPTION PREMIUM", "Netflix",
                new BigDecimal("649.00"), today.minusDays(10), TransactionType.DEBIT,
                TransactionCategory.ENTERTAINMENT, "Streaming", 0.99,
                "Digital video entertainment subscription."));

        samples.add(createSample("BOOKMYSHOW CINEMA TICKETS PVR", "BookMyShow",
                new BigDecimal("820.00"), today.minusDays(3), TransactionType.DEBIT,
                TransactionCategory.ENTERTAINMENT, "Movies", 0.98,
                "Movie tickets booked through BookMyShow."));

        samples.add(createSample("SPOTIFY MONTHLY PREMIUM INDIVIDUAL", "Spotify",
                new BigDecimal("119.00"), today.minusDays(18), TransactionType.DEBIT,
                TransactionCategory.ENTERTAINMENT, "Music Streaming", 0.99,
                "Audio streaming platform subscription."));

        // 6. Healthcare
        samples.add(createSample("APOLLO PHARMACY MEDICALS", "Apollo Pharmacy",
                new BigDecimal("650.00"), today.minusDays(5), TransactionType.DEBIT,
                TransactionCategory.HEALTHCARE, "Pharmacy", 0.98,
                "Medicines and healthcare supplies purchased at Apollo Pharmacy."));

        samples.add(createSample("PRACTO CONSULTATION DR SHARMA", "Practo",
                new BigDecimal("800.00"), today.minusDays(11), TransactionType.DEBIT,
                TransactionCategory.HEALTHCARE, "Doctor Consultation", 0.96,
                "Medical consultation booked via Practo healthcare platform."));

        // 7. Education
        samples.add(createSample("UDEMY ONLINE COURSE PYTHON & JAVA", "Udemy",
                new BigDecimal("499.00"), today.minusDays(7), TransactionType.DEBIT,
                TransactionCategory.EDUCATION, "Online Learning", 0.97,
                "Professional software engineering course purchase."));

        samples.add(createSample("OXFORD BOOKSTORE TECHNICAL BOOKS", "Oxford Bookstore",
                new BigDecimal("1200.00"), today.minusDays(20), TransactionType.DEBIT,
                TransactionCategory.EDUCATION, "Books", 0.95,
                "Academic textbooks and literature."));

        // 8. Rent
        samples.add(createSample("NEFT/RENT/OCTOBER APARTMENT 402", "Landlord Rental",
                new BigDecimal("18500.00"), today.minusDays(17), TransactionType.DEBIT,
                TransactionCategory.RENT, "Apartment Rent", 0.99,
                "Monthly apartment lease and rental payment."));

        // 9. Transfers
        samples.add(createSample("IMPS/P2P/TRANSFER TO RAHUL SHARMA", "Rahul Sharma",
                new BigDecimal("2500.00"), today.minusDays(6), TransactionType.DEBIT,
                TransactionCategory.TRANSFERS, "Peer-to-Peer", 0.95,
                "Direct bank transfer to personal contact."));

        samples.add(createSample("UPI/PAYTM/SENT TO SISTER", "Paytm Transfer",
                new BigDecimal("1000.00"), today.minusDays(13), TransactionType.DEBIT,
                TransactionCategory.TRANSFERS, "Family Transfer", 0.94,
                "Funds transferred to family member via UPI."));

        // 10. Ambiguous / Other Transactions
        samples.add(createSample("POS-TXN-90281 SHOP NO 42 BLR", "Unknown Retailer",
                new BigDecimal("320.00"), today.minusDays(9), TransactionType.DEBIT,
                TransactionCategory.OTHER, "General Merchant", 0.40,
                "Ambiguous POS terminal transaction without identifiable merchant name."));

        samples.add(createSample("ACH-DEBIT MISC CHARGE 9920", "Unknown Bank Charge",
                new BigDecimal("75.00"), today.minusDays(16), TransactionType.DEBIT,
                TransactionCategory.OTHER, "Bank Fee", 0.45,
                "Unspecified bank debit code, classified as Other."));

        // 11. Income / Credits
        samples.add(createSample("NEFT/SALARY/TECHCORP SERVICES NOV", "TechCorp Services",
                new BigDecimal("65000.00"), today.minusDays(17), TransactionType.CREDIT,
                TransactionCategory.TRANSFERS, "Monthly Salary", 0.99,
                "Direct deposit monthly employment salary."));

        samples.add(createSample("UPI/CREDIT/FREELANCE DESIGN INVOICE 12", "Client Payment",
                new BigDecimal("12500.00"), today.minusDays(8), TransactionType.CREDIT,
                TransactionCategory.TRANSFERS, "Freelance Income", 0.96,
                "Consulting service compensation received."));

        samples.add(createSample("INTEREST CREDIT QUARTERLY SAVINGS", "Bank Interest",
                new BigDecimal("420.00"), today.minusDays(19), TransactionType.CREDIT,
                TransactionCategory.TRANSFERS, "Bank Interest", 0.98,
                "Quarterly savings bank interest credit."));

        samples.add(createSample("CASHBACK REWARD CREDITED CRED", "CRED Rewards",
                new BigDecimal("150.00"), today.minusDays(10), TransactionType.CREDIT,
                TransactionCategory.TRANSFERS, "Cashback", 0.95,
                "Financial app cashback bonus."));

        transactionRepository.saveAll(samples);
        log.info("Successfully loaded {} fictional sample transactions into H2 database.", samples.size());
        return samples.size();
    }

    private Transaction createSample(String raw, String merchant, BigDecimal amount,
                                     LocalDate date, TransactionType type,
                                     TransactionCategory cat, String subcat,
                                     double confidence, String explanation) {
        Transaction tx = new Transaction(raw, merchant, amount, date, type, cat, subcat, confidence, explanation);
        return tx;
    }
}
