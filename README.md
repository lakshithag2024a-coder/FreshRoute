# FreshRoute: Smart Surplus Food Rescue Web Application

**FreshRoute** is a production-grade full-stack web application designed to connect food donors (restaurants, bakeries, grocery stores, and college dining halls) with community food rescue recipients (shelters, food pantries, NGOs, and volunteers) to claim and collect surplus edible food before pickup deadlines expire, preventing avoidable food waste.

---

## 1. Technology Stack

- **Java Version:** Java 21 / OpenJDK 25 LTS
- **Framework:** Spring Boot 3.3.5
- **Web MVC:** Spring MVC with Thymeleaf Template Engine
- **Data Persistence:** Spring Data JPA & Hibernate
- **Database:** H2 Database in Persistent File Mode (`./data/freshroutedb`)
- **Security:** Spring Security with BCrypt Password Hashing and Role-Based Access Control
- **Validation:** Jakarta Validation (`@Valid`, `@Future`, `@NotBlank`, `@Min`)
- **Frontend:** HTML5, CSS3, Bootstrap 5.3, Bootstrap Icons, and Vanilla JavaScript
- **Data Visualizations:** Chart.js 4.4
- **Build Tool:** Apache Maven 3.9.9 with included Maven Wrapper (`mvnw.cmd`)

---

## 2. Architecture & Design Pattern

FreshRoute follows a strict layered enterprise architecture:

```
Web Client (Browser)
       ↓
Spring MVC Controllers (Presentation & Route Handling)
       ↓
Service Layer (Deterministic Business Logic & Transactions)
       ↓
Spring Data JPA Repositories (Data Access)
       ↓
Persistent H2 Database Engine (File-backed storage)
```

- **Domain Models / Entities:** `User`, `FoodListing`, `Reservation`
- **Enums:** `Role` (ROLE_DONOR, ROLE_RECIPIENT, ROLE_ADMIN), `FoodCategory`, `ListingStatus`, `ReservationStatus`, `FoodUnit`
- **Data Transfer Objects (DTOs):** `UserRegistrationDto`, `FoodListingDto`, `ReservationRequestDto`, `MatchingCriteriaDto`, `SmartMatchResultDto`, `UserProfileDto`, `ImpactMetricsDto`

---

## 3. Demonstration Credentials

FreshRoute automatically initializes realistic sample accounts and listings on first launch without overwriting existing data on subsequent runs:

| Role | Username | Password | Organization / Name | Details |
| :--- | :--- | :--- | :--- | :--- |
| **Donor** | `greenleaf` | `password123` | Green Leaf Bistro | Casual dining restaurant |
| **Donor** | `downtownbakery` | `password123` | Downtown Artisan Bakery | Bakery with sourdough & pastries |
| **Donor** | `citysupermarket` | `password123` | City Fresh Supermarket | Grocery store with produce & dairy |
| **Donor** | `campusmess` | `password123` | Tech University Dining Hall | High-volume student campus mess |
| **Recipient** | `hopeshelter` | `password123` | Hope Community Shelter | Emergency community shelter |
| **Recipient** | `sunshineyouth` | `password123` | Sunshine Youth Outreach | Non-profit community youth hub |
| **Recipient** | `foodforall` | `password123` | Food For All Volunteers | Grassroots food rescue NGO |
| **Recipient** | `volunteer_maya` | `password123` | Mission District Community Pantry | Volunteer neighborhood organizer |
| **Admin** | `admin` | `admin123` | FreshRoute Coordination | Full administrative oversight |

> *Note: One-click autofill buttons are built directly into the `/login` page for fast local evaluation.*

---

## 4. Key Functional Modules

### Module 1: Authentication & Role-Based Access
- Secure registration for Donors and Recipients with organization, contact, and address details.
- Spring Security session management with BCrypt password hashing.
- Role-based navigation and route guards (Donors cannot modify other donors' listings; Recipients cannot access donor management; unauthorized attempts redirect to custom 403 page).

### Module 2: Donor Portal
- Interactive dashboard showing active listings, pending claim requests, and rescued meal counts.
- Create new food listing with strict future deadline validation, category categorization, and mandatory food-safety declaration.
- Edit and delete listings with ownership verification.
- Real-time listing status tracking (`AVAILABLE`, `RESERVED`, `COLLECTED`, `EXPIRED`, `CANCELLED`).

### Module 3: Surplus Food Marketplace
- Publicly browsable catalog for authenticated community recipients.
- Keyword search across title, description, and pickup locations.
- Food category filtering (Cooked Meals, Bakery, Produce, Packaged Food, Dairy & Beverages).
- Urgency-based sorting with live client countdown timers (`data-deadline`).
- Automated exclusion of expired and fully claimed surplus batches.

### Module 4: Smart Matching Engine
- Rule-based algorithmic ranking in Java calculating a normalized compatibility score (0 to 100):
  - **Urgency Dimension (40% default):** Prioritizes surplus nearing its disposal deadline (&lt; 2h receives 100).
  - **Capacity Fit Dimension (35% default):** Evaluates requested portion demand against available batch supply.
  - **Transit Feasibility Dimension (25% default):** Evaluates distance feasibility to minimize transit emissions.
- Provides human-readable explanation tags for each matched batch (e.g. `⚡ Critical Urgency`, `🎯 Perfect Quantity Match`, `📍 Close Proximity`).
- Configurable scoring weights and priority modes (`URGENT_FIRST`, `CAPACITY_FIRST`, `DISTANCE_FIRST`, `BALANCED`).

### Module 5: Reservations & Pickup Logistics
- Recipients reserve specific quantities up to available amounts.
- Unique verification pass code generation (e.g. `FR-2026-X8K9`).
- Synchronized transactional inventory reservation to prevent overbooking.
- Duplicate active reservation prevention.
- Seamless cancellation workflow that immediately restores unclaimed inventory to the marketplace.
- Donor confirmation and handover recording workflow.

### Module 6: Food Safety & Expiry Management
- Mandatory food-safety declaration checkbox required on all listings.
- Automated server-side scheduled sweep (`@Scheduled(fixedRate = 300000)`) updating listings to `EXPIRED`.
- Color-coded urgency badges (&lt; 2h red pulse, &lt; 6h amber warning).
- Prominent food-safety framework disclaimers across marketplace and detail views.

### Module 7: Live Impact Analytics
- Realtime database-backed aggregation of total food batches, active available surplus, completed pickups, and portions saved.
- **Environmental Impact Model:**
  - $\text{Food Waste Avoided (kg)} = \text{Rescued Portions} \times 0.50\text{ kg/meal}$
  - $\text{CO}_2\text{e Avoided (kg)} = \text{Food Waste Avoided (kg)} \times 2.50\text{ kg CO}_2\text{e/kg}$
- Chart.js visualizations for status distribution, category volume, and monthly rescue trends.

---

## 5. Local Setup & Execution Instructions

### Prerequisites
- JDK 21 or compatible installed Java (JDK 25 LTS).
- Port `8080` available locally.

### How to Run

1. Open PowerShell / Command Prompt and navigate to the project directory:
   ```powershell
   cd C:\Users\11lak\.gemini\antigravity\scratch\freshroute
   ```

2. Package and compile the project using the included Maven Wrapper:
   ```powershell
   .\mvnw.cmd package -DskipTests
   ```

3. Launch the Spring Boot application:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
   *(Or run the generated jar: `java -jar target/freshroute-1.0.0.jar`)*

4. Open your browser and navigate to:
   - **FreshRoute Application:** [http://localhost:8080](http://localhost:8080)
   - **Surplus Marketplace:** [http://localhost:8080/marketplace](http://localhost:8080/marketplace)
   - **Smart Matcher:** [http://localhost:8080/recipient/smart-matches](http://localhost:8080/recipient/smart-matches)
   - **Impact Dashboard:** [http://localhost:8080/impact](http://localhost:8080/impact)
   - **H2 Database Console:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
     - JDBC URL: `jdbc:h2:file:./data/freshroutedb`
     - Username: `sa`
     - Password: *(leave blank)*

---

## 6. Verification Checklist

- [x] Persistent H2 storage across application restarts in `./data/freshroutedb`
- [x] Secure BCrypt password hashing & authentication
- [x] Role-based page access and navigation for DONOR, RECIPIENT, and ADMIN
- [x] Surplus listing publishing, updating, and deletion with ownership authorization
- [x] Public surplus marketplace with keyword search, category filtering, and urgency sorting
- [x] Rule-based Smart Matching Engine with normalized 0-100 scoring & explanation reasons
- [x] Reservation workflow with unique pass codes, quantity validation, and cancellation inventory restoration
- [x] Donor handover confirmation & collection completion
- [x] Automated expiry tracking based on authoritative server clock
- [x] Live impact calculations and Chart.js visualizations
- [x] 20 distinct functional Thymeleaf web pages
