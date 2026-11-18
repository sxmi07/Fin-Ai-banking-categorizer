# FinAI — Intelligent Banking Transaction Categorizer
## 🚀 Live Demo
🔗 **[Open Live FinAI App](https://YOUR-RENDER-LINK.onrender.com)**  
*(Hosted on Render Free Tier — first load takes ~30–40 seconds while the server wakes up.)*

[![Java 17](https://img.shields.io/badge/Java-17-orange.svg)](https://adoptium.net/)
[![Spring Boot 3.2.5](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Google Gemini](https://img.shields.io/badge/AI-Google%20Gemini%201.5%20Flash-blue.svg)](https://aistudio.google.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> **Transforming Cryptic Banking Transactions into Structured Financial Insights.**  
> Built with Java 17, Spring Boot 3, Google Gemini 1.5 Flash Cloud LLM, and a modern Stripe/Ramp-inspired fintech interface.

---

## 📌 Project Overview

**FinAI** is an AI-powered financial categorization engine that solves one of the most frustrating problems in personal finance: **cryptic bank statement descriptions**.

When you download your bank statement or UPI transaction history, you often see confusing raw strings such as:
- `UPI/409283/SWIGGY BANGALORE`
- `POS 402910 DMART MUMBAI`
- `NEFT-N1029384-SALARY-ACME TECH`
- `BILLDESK BESCOM ELEC 98234`

To the human eye or standard spreadsheet formulas, these strings are inconsistent and hard to categorize. **FinAI** uses **Google Gemini 1.5 Flash Cloud LLM** paired with a resilient **Rule-Based Heuristic Fallback Engine** to automatically:
1. Clean the raw text and extract the true **Merchant Name** (e.g., `Swiggy`, `DMart`, `BESCOM`).
2. Assign one of 10 standard financial categories (Food, Travel, Utilities, Shopping, Entertainment, Healthcare, Education, Rent, Transfers, Other).
3. Compute a **Confidence Score** (0.0 to 1.0) and generate an **Audit Explanation** for transparency.
4. Allow one-click **Manual Category Correction** that updates summaries and dashboards in real-time.
5. Provide interactive **Chart.js analytics**, **CSV statement import**, and **CSV export**.

---

## 🎯 Problem Statement & Solution

| The Problem | How FinAI Solves It |
| :--- | :--- |
| **Cryptic Bank Codes**: Descriptions contain bank reference numbers, terminal IDs, and garbled abbreviations. | **Smart NER & Prompt Engineering**: LLM and regex patterns strip noise and identify the underlying merchant. |
| **Inaccurate Categorization**: Static keyword lookups fail on slang, abbreviations, or new merchants. | **Hybrid Intelligence**: Google Gemini 1.5 Flash parses context with natural language reasoning. |
| **Zero Downtime Requirement**: If an API quota is exhausted or internet drops, apps fail. | **Heuristic Fallback Engine**: 100% offline rule-based regex fallback guarantees zero downtime. |
| **Low Hardware Resources**: Local LLMs (Ollama, Llama-3) require 8GB-16GB RAM and dedicated GPUs. | **Cloud AI + Embedded H2**: Capped at **512MB RAM**, runs smoothly on budget **4GB RAM laptops**. |

---

## 🏛️ Architecture Explained Simply (For Student & Academic Presentations)

FinAI is designed as a clean **3-Tier Lightweight Monolithic Architecture**:

```
┌────────────────────────────────────────────────────────────────────────┐
│                   1. PRESENTATION LAYER (Client Browser)                │
│   • Single Page Application (HTML5, CSS3, Bootstrap 5, Vanilla JS)      │
│   • Views: Dashboard, Transactions, AI Categorizer, Analytics, Settings │
│   • Interactive Visualizations: Chart.js (Doughnut, Bar, Trend Lines)  │
└───────────────────────────────────▲────────────────────────────────────┘
                                    │ HTTP / REST (JSON)
┌───────────────────────────────────▼────────────────────────────────────┐
│                  2. APPLICATION LAYER (Spring Boot 3 Core)             │
│                                                                        │
│   REST Controllers ────► TransactionService ───► CsvService            │
│                                 │                                      │
│                                 ▼                                      │
│                     AiCategorizationService                            │
│                     (Hybrid Routing Engine)                            │
│                      /                   \                             │
│       [Primary: Online]                 [Secondary: 100% Offline]      │
│     GeminiAiService                      RuleBasedFallbackService      │
│  (Google Gemini 1.5 Flash)               (Regex & Keyword Heuristics)  │
│    • RestClient HTTP API                   • 10 Categories Handled     │
│    • Strict JSON Output                    • Confidence: 0.85 - 0.95   │
│    • Prompt Injection Guard                • Zero-Latency Fallback     │
└───────────────────────────────────▲────────────────────────────────────┘
                                    │ JPA / Hibernate
┌───────────────────────────────────▼────────────────────────────────────┐
│                    3. DATA LAYER (Embedded Persistence)                │
│   • Spring Data JPA Repositories                                       │
│   • Embedded H2 File-Backed Database (`./data/finaidb.mv.db`)          │
│   • Pre-seeded with 30 Realistic Indian Banking Demo Transactions      │
└────────────────────────────────────────────────────────────────────────┘
```

### Why This Design is Ideal for Student Demos:
1. **Zero External Software Setup**: No need to install MySQL, PostgreSQL, Docker, or Ollama. The entire application boots from a single Maven command.
2. **Strict Memory Protection**: The JVM is locked to `<argLine>-Xmx512m</argLine>`, using less than 15% of a 4GB RAM machine.
3. **Graceful Degradation**: If you don't have a Gemini API key or internet access during a live presentation, the app seamlessly runs on its built-in rule engine without displaying any error screens!

---

## 💻 Tech Stack

| Layer | Technologies |
| :--- | :--- |
| **Backend** | Java 17, Spring Boot 3.2.5, Spring Web, Spring Data JPA, Hibernate, Bean Validation |
| **Cloud AI** | Google Gemini 1.5 Flash (`generateContent` REST API), Custom Structured JSON Prompts |
| **Database** | Embedded H2 Database (File-backed mode with H2 Console) |
| **Frontend** | HTML5, CSS3 (Modern SaaS Theme), Bootstrap 5, Vanilla JavaScript (ES6+), Chart.js |
| **Testing** | JUnit 5, Dynamic Java Proxies & Lightweight Stubs (Fast, 0 ByteBuddy Agent overhead) |
| **Build & Tooling** | Apache Maven 3.10.1, Git, JVM Memory-Tuned Configuration |

---

## 📁 Project Folder Structure

```
finAI/
├── .mvn/
│   └── jvm.config                     # Memory cap flags (-Xms128m -Xmx512m)
├── src/
│   ├── main/
│   │   ├── java/com/finai/
│   │   │   ├── FinAiApplication.java        # Spring Boot entry point
│   │   │   ├── ai/                          # AI & Heuristic Engines
│   │   │   │   ├── AiCategorizationService.java       # Hybrid coordinator (Gemini + Fallback)
│   │   │   │   ├── CategorizationPromptTemplate.java  # Anti-injection structured prompt
│   │   │   │   ├── GeminiAiService.java               # Google Gemini REST client
│   │   │   │   └── RuleBasedFallbackService.java      # Offline regex/keyword heuristic engine
│   │   │   ├── config/                      # App configuration & demo seed loader
│   │   │   │   └── DemoDataLoader.java                # Seeds 30 realistic transactions
│   │   │   ├── controller/                  # REST API Endpoints
│   │   │   │   ├── DemoController.java                # Reset database to demo data
│   │   │   │   ├── HealthController.java              # System telemetry & AI provider status
│   │   │   │   └── TransactionController.java         # CRUD, categorization, CSV import/export
│   │   │   ├── dto/                         # Data Transfer Objects
│   │   │   │   ├── ApiResponse.java                   # Standard API wrapper
│   │   │   │   ├── CategorizationResultDTO.java       # AI categorization output
│   │   │   │   ├── CategorizeRequestDTO.java          # Batch AI request
│   │   │   │   ├── CategoryUpdateRequestDTO.java      # Category manual override request
│   │   │   │   ├── TransactionRequestDTO.java         # New transaction request
│   │   │   │   ├── TransactionResponseDTO.java        # Transaction response entity
│   │   │   │   └── TransactionSummaryDTO.java         # Financial KPI & monthly stats
│   │   │   ├── exception/                   # Global exception handling
│   │   │   │   ├── ErrorResponse.java
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   └── ResourceNotFoundException.java
│   │   │   ├── model/                       # JPA Database Entities & Enums
│   │   │   │   ├── Transaction.java                   # Main JPA entity with database indexes
│   │   │   │   ├── TransactionCategory.java           # 10 standard financial categories
│   │   │   │   └── TransactionType.java               # DEBIT / CREDIT enum
│   │   │   └── service/                     # Business logic services
│   │   │       ├── CsvService.java                    # CSV statement parser & exporter
│   │   │       └── TransactionService.java            # CRUD, analytics, & AI integration
│   │   └── resources/
│   │       ├── application.properties       # Port, H2, and Gemini configuration
│   │       └── static/                      # Modern Fintech SPA Frontend
│   │           ├── index.html                         # Multi-view Single Page Application
│   │           ├── css/styles.css                     # Stripe/Ramp SaaS stylesheet
│   │           └── js/app.js                          # SPA router, API wiring, Chart.js logic
│   └── test/
│       └── java/com/finai/                  # 36 Lightweight JUnit 5 automated tests
│           ├── FinAiApplicationTests.java
│           ├── ai/CategorizationPromptTemplateTest.java
│           ├── ai/RuleBasedFallbackServiceTest.java
│           ├── controller/DemoControllerTest.java
│           ├── controller/HealthControllerTest.java
│           ├── controller/TransactionControllerTest.java
│           ├── service/CsvServiceTest.java
│           └── service/TransactionServiceTest.java
├── .env.example                             # Environment variable template
├── .gitignore                               # Clean git ignore configuration
├── pom.xml                                  # Maven dependencies & test heap limit
└── README.md                                # Comprehensive project documentation
```

---

## 🔌 REST API Specification

All API responses are wrapped in a standardized JSON envelope:
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... },
  "status": 200,
  "timestamp": "2026-11-18T14:30:00"
}
```

### Complete Endpoints Table

| Method | Endpoint | Description | Request Body / Query Params |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/transactions` | Paginated, searchable, and filtered transactions | `?search=&category=&type=&startDate=&endDate=&page=0&size=15&sortBy=transactionDate&sortDir=DESC` |
| `GET` | `/api/transactions/{id}` | Get single transaction by ID | *None* |
| `POST` | `/api/transactions` | Create a new transaction manually | `{"description": "...", "amount": 450.00, "date": "2026-11-18", "type": "DEBIT"}` |
| `PUT` | `/api/transactions/{id}/category` | Manually update transaction category | `{"category": "Food", "subcategory": "Groceries"}` |
| `DELETE` | `/api/transactions/{id}` | Delete a transaction | *None* |
| `GET` | `/api/transactions/summary` | Get financial KPIs, category distribution, & savings | `?year=2026&month=11` (Optional) |
| `POST` | `/api/transactions/categorize` | Categorize raw descriptions via AI engine | `{"descriptions": ["UPI/409283/SWIGGY BANGALORE", "AIRTEL BROADBAND"]}` |
| `POST` | `/api/transactions/import` | Upload bank statement CSV, categorize, & save | Multipart Form (`file: statement.csv`) |
| `GET` | `/api/transactions/export` | Download all categorized transactions as CSV | *None* (Returns CSV binary stream) |
| `GET` | `/api/health` | Health status, AI engine status, & JVM memory stats | *None* |
| `POST` | `/api/demo/reset` | Reset H2 database back to 30 sample transactions | *None* |

---

## 🔑 Google Gemini API Setup (Optional)

FinAI runs **100% offline out-of-the-box** using its intelligent rule-based engine. To activate live Google Gemini Cloud LLM categorization:

1. Visit [Google AI Studio](https://aistudio.google.com/) and sign in with your Google account.
2. Click **"Get API key"** and create a new free API key.
3. Configure the key using either of these two methods:

### Method A: Environment Variable (Recommended)
Set the variable in PowerShell:
```powershell
$env:GEMINI_API_KEY="AIzaSyYourActualKeyHere"
```
Or in Command Prompt / Bash:
```bash
export GEMINI_API_KEY="AIzaSyYourActualKeyHere"
```

### Method B: Application Properties
Edit `src/main/resources/application.properties`:
```properties
gemini.api.key=AIzaSyYourActualKeyHere
```

> **Note**: If no API key is provided, FinAI automatically runs in `DEMO_FALLBACK_MODE` using the rule-based engine with zero errors!

---

## 🚀 How to Build, Test, and Run

### Prerequisites
- **Java 17 JDK** installed and added to `PATH` (`java -version`).
- **Apache Maven 3.8+** installed (`mvn -v`).

### 1. Compile the Project
Verify that the project compiles cleanly:
```bash
mvn clean compile
```

### 2. Run Automated Unit Tests
Run the 36 lightweight JUnit 5 unit tests (memory-capped to 512MB):
```bash
mvn test
```
*Expected result:* `Tests run: 36, Failures: 0, Errors: 0, Skipped: 0` (`BUILD SUCCESS`).

### 3. Start the Spring Boot Application
```bash
mvn spring-boot:run
```

Once started:
- 🌐 **Web Application**: Open [http://localhost:8080](http://localhost:8080) in any browser.
- 🩺 **Health Telemetry**: Check [http://localhost:8080/api/health](http://localhost:8080/api/health).
- 🗄️ **H2 Database Console**: Visit [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  - **JDBC URL**: `jdbc:h2:file:./data/finaidb`
  - **Username**: `sa`
  - **Password**: *(leave blank)*

---

## 📊 Pre-Loaded Demonstration Data

FinAI automatically initializes the database with **30 realistic Indian banking transactions** on first launch, covering:
- **Food Delivery & Dining**: Swiggy, Zomato, Starbucks, McDonald's
- **E-Commerce & Shopping**: Amazon Pay, Flipkart, Myntra, Zara
- **Utilities**: BESCOM Electricity, Tata Power, Airtel Broadband, Indane Gas
- **Travel & Commute**: Uber, Ola Cabs, IRCTC Railway Booking, IndiGo Airlines
- **Entertainment**: Netflix, Spotify, BookMyShow, Disney+ Hotstar
- **Healthcare**: Apollo Pharmacy, Practo Consultation, 1mg Medicines
- **Income & Payroll**: Tech Mahindra Salary, Consulting Fee, Dividend Payout

To reset the database back to these original 30 records at any time, navigate to the **Settings** view in the UI and click **"Reset Database to 30 Demo Transactions"**, or execute:
```bash
curl -X POST http://localhost:8080/api/demo/reset
```

---

## 🛡️ Security & Privacy Highlights

- **Untrusted Input Sanitation**: Transaction descriptions are treated as untrusted user data. Prompt templates use strict delimiter boundaries and instructions that prevent prompt injection or system prompt leaking.
- **Privacy Masking**: Raw account numbers, card numbers, and IFSC codes are stripped before sending to LLM APIs.
- **Zero Cloud Storage**: All financial records remain stored strictly on your local machine in the embedded H2 file database.

---

## 📄 License

This project is open-source and available under the [MIT License](LICENSE).
