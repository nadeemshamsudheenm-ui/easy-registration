# EASY REGISTRATION — Online Semester Registration & Approval Management System

A complete, beginner-friendly Java web application built with **Spring Boot 3**, **Spring Data JPA**, **MySQL**, and a responsive frontend using **Vanilla HTML5, CSS3, and JavaScript**.

---

## 🌟 Key Features

1. **Student Self-Service Portal**:
   - Register and login using Register Number and password.
   - Fill and submit semester registration forms including CGPA, backlogs, tuition fee receipt details, and registered courses.
   - Declare whether college transport (bus) is opted (with route & boarding point).
   - Real-time interactive multi-stage visual stepper tracking departmental clearances.
   - Printable / downloadable official **Semester Registration & Clearance Card** with barcode and departmental seals once approved.

2. **Accounts Department Desk**:
   - Reviews pending semester applications.
   - Verifies tuition fee payment receipt numbers and amounts.
   - Grants clearance; the system **automatically forwards** the application to Transport (if the student uses the bus) or directly to Laboratory (if transport is not opted).

3. **Transport Department Desk**:
   - Reviews students availing college transportation.
   - Verifies bus route, stop, and bus fee pass receipt.
   - Automatically forwards the application to Laboratory clearance upon approval.

4. **Laboratory Assistant Desk**:
   - Checks laboratory hardware kit returns, library records, and breakage dues.
   - Issues "No Dues" clearance with remarks.
   - Automatically forwards to the Head of Department (HOD) for final sign-off.

5. **HOD (Head of Department) Desk**:
   - Reviews academic standing (CGPA, backlogs, curriculum).
   - Verifies that all prior departmental clearances (Accounts, Transport, Lab) have been satisfied.
   - Grants final approval or sends the application back with remarks.
   - Transitions application status to `APPROVED`, unlocking the printable clearance slip for the student.

6. **Automatic Forwarding State Machine**:
   - Applications transition automatically through:
     $$\text{Draft} \longrightarrow \text{Pending Accounts} \longrightarrow \begin{cases} \text{Pending Transport} & \text{if transport opted} \\ \text{Pending Lab} & \text{if not opted} \end{cases} \longrightarrow \text{Pending Lab} \longrightarrow \text{Pending HOD} \longrightarrow \text{APPROVED}$$

---

## 🛠️ Technology Stack & OOP Concepts

- **Backend**: Java 17, Spring Boot 3.2.x, Spring Data JPA, Hibernate, Jakarta Validation
- **Database**: MySQL 8.x (with automatic schema creation and seed data initialization)
- **Frontend**: HTML5, Vanilla CSS3 (Custom Responsive Academic Portal Design System), Vanilla ES6 JavaScript
- **Object-Oriented Concepts**:
  - **Encapsulation**: Private state in `User`, `RegistrationApplication`, and `ApprovalAuditLog` with getter/setter validations.
  - **Inheritance & Hierarchy**: Custom domain exceptions (`BusinessException`, `GlobalExceptionHandler`).
  - **Abstraction**: Clean separation of concerns across Controller $\rightarrow$ Service $\rightarrow$ Repository $\rightarrow$ Database layers.
  - **Polymorphic State Transitions**: Role-based workflow processing in `ApprovalWorkflowService`.

---

## 🔑 Pre-Configured Test Credentials

On application startup, `DataInitializer` automatically provisions demo accounts for every role:

| Role | Username / ID | Password | Purpose |
| :--- | :--- | :--- | :--- |
| **Student** | `2024CS101` | `password123` | Student with active application waiting at Accounts |
| **Student (Approved)** | `2024CS103` | `password123` | Student with fully approved slip ready to print |
| **Student (New)** | `2024CS102` | `password123` | Student with clean slate to submit a fresh registration |
| **Accounts Staff** | `accounts_admin` | `password123` | Verifies tuition fees and receipts |
| **Transport Staff** | `transport_admin` | `password123` | Verifies bus routes and passes |
| **Lab Assistant** | `lab_admin` | `password123` | Issues lab and equipment clearance |
| **Head of Dept (HOD)** | `hod_cs` | `password123` | Final academic sign-off and approval |

---

## ⚙️ How to Configure and Run Locally

### Prerequisites
- **Java JDK 17** or newer installed (`java -version`)
- **Apache Maven 3.8+** installed (`mvn -version`)
- **MySQL Server** installed and running on port `3306`

### 1. Configure MySQL Database
Open `src/main/resources/application.properties` and verify your MySQL username and password:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/easyregistration?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
```

> **Note**: `createDatabaseIfNotExist=true` ensures MySQL automatically creates the `easyregistration` database upon startup. You do not need to manually run SQL scripts, though `schema.sql` is provided for reference.

### 2. Build the Project
Open PowerShell or Terminal in the project root directory (`Easyregistration`):

```bash
mvn clean package -DskipTests
```

### 3. Run the Application
Execute the Spring Boot runner:

```bash
mvn spring-boot:run
```

Or run the generated JAR:

```bash
java -jar target/easy-registration-1.0.0.jar
```

### 4. Open in Browser
Open your browser and navigate to:
```
http://localhost:8080
```

---

## 📋 Recommended Workflow Test Walkthrough

1. **Explore the Landing Page**:
   - Open `http://localhost:8080/`. You will see the college portal with quick test cards.
2. **Review Accounts Clearance**:
   - Log in as **Accounts Staff** (`accounts_admin` / `password123`).
   - You will see student `Aravind Krishna`'s application (`REG-2026-S4-1001`) in your queue.
   - Click **Review & Clear →**, inspect the tuition fee details, and click **Verify & Grant Clearance**.
   - Notice the application disappears from Accounts and **automatically forwards** to Transport!
3. **Review Transport Clearance**:
   - Switch user to **Transport Staff** (`transport_admin` / `password123`).
   - The application is now in the Transport queue. Verify the bus route and click **Verify & Grant Clearance**.
   - Application is **automatically forwarded** to Laboratory!
4. **Review Lab Clearance**:
   - Log in as **Lab Assistant** (`lab_admin` / `password123`).
   - Click **Verify & Grant Clearance** to issue no-dues clearance.
   - Application forwards to HOD!
5. **Grant HOD Final Approval**:
   - Log in as **HOD** (`hod_cs` / `password123`).
   - Review all prerequisite clearance seals and click **Verify & Grant Clearance**.
   - The status is now `APPROVED`.
6. **Print Registration Slip**:
   - Log in as Student `2024CS101` (or `2024CS103`).
   - The multi-stage stepper shows all steps completed green.
   - Click **Download & Print Clearance Slip** to view the official printable clearance card with college header and barcodes!
