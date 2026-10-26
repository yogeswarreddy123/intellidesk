# IntelliDesk — AI-Powered IT Service Management System

**IntelliDesk** is an enterprise-grade full-stack IT Service Management (ITSM) and help-desk platform designed for corporate environments. It streamlines incident reporting, self-service knowledge discovery, and support resolution with artificial intelligence assistance and heuristic ticket classification.

---

## 🚀 Key Features

### 👤 Employee Self-Service Portal
- **Ticket Lifecycle**: Log incidents, view personal ticket status history, add commentary, and confirm ticket resolution.
- **AI Assistant**: 24/7 conversational troubleshooting engine that searches the internal knowledge base, delivers step-by-step resolution advice, and generates pre-filled tickets in one click.
- **Smart AI Ticket Classification**: Analyzes issue title and description in real time to suggest the most appropriate Category and Urgency/Priority level before submission.
- **Knowledge Base Search**: Instantly search verified troubleshooting guides for VPN, Wi-Fi, printer queues, email sync, and software access.

### 🛠️ IT Support Agent Workspace
- **Queue Management**: View assigned tickets, unassigned tickets, and all department incidents with real-time status badges.
- **Incident Triaging**: Accept tickets, adjust priority levels, update statuses (`OPEN`, `IN_PROGRESS`, `PENDING`, `RESOLVED`, `CLOSED`), and reassign between specialized IT teams.
- **Collaborative Threading**: Communicate directly with employees via ticket activity comments.
- **Author Knowledge Articles**: Publish and update verified IT solutions directly into the knowledge base.

### 🛡️ Administrative Control & Governance
- **User Management**: Add, update, activate/deactivate accounts, assign departments, and switch roles between `EMPLOYEE`, `IT_AGENT`, and `ADMIN`.
- **Category & Department Management**: Full CRUD configuration for IT departments (Network, Security, Database, IT Support, etc.) and incident categories.
- **Executive Analytics & Reports**: Live metrics for Total Tickets, Open Backlog, Resolved Incidents, Critical Outages, and Average Resolution Time (hours), alongside distribution graphs by Category, Priority, Status, and Month.
- **Immutable Audit Logging**: Records critical events (`LOGIN`, `CREATE_TICKET`, `UPDATE_TICKET`, `ASSIGN_TICKET`, `CHANGE_STATUS`, `DELETE_USER`) with timestamps and user identifiers.

---

## 🏗️ Architecture

```text
       ┌─────────────────────────────────────────────────────────┐
       │   Frontend: HTML5, CSS3, Vanilla JavaScript, Fetch API   │
       │   (Responsive Enterprise Dashboard & Mobile Navigation)  │
       └───────────────────────────┬─────────────────────────────┘
                                   │ HTTP / JSON (REST + JWT Bearer)
                                   ▼
       ┌─────────────────────────────────────────────────────────┐
       │             Spring Boot 3 REST Controllers              │
       │    (Auth, Users, Tickets, Comments, Knowledge, AI)     │
       └───────────────────────────┬─────────────────────────────┘
                                   │
       ┌───────────────────────────▼─────────────────────────────┐
       │                   Spring Security 6                     │
       │    (BCrypt Password Hashing, JWT Token Verification,    │
       │     Role-Based Access Control: ADMIN, IT_AGENT, EMP)    │
       └───────────────────────────┬─────────────────────────────┘
                                   │
       ┌───────────────────────────▼─────────────────────────────┐
       │              Service & Business Logic Layer             │
       │  (AiAssistantService, TicketClassificationService, etc) │
       └───────────────────────────┬─────────────────────────────┘
                                   │
       ┌───────────────────────────▼─────────────────────────────┐
       │           Spring Data JPA & Hibernate ORM               │
       └───────────────────────────┬─────────────────────────────┘
                                   │
       ┌───────────────────────────▼─────────────────────────────┐
       │                  MySQL Database                         │
       │     (Tables: users, tickets, comments, articles, etc)   │
       └─────────────────────────────────────────────────────────┘
```

---

## 💻 Tech Stack

- **Backend**:
  - Java 17+ (JDK 17, 21, or 24)
  - Spring Boot 3.2.5
  - Spring Security 6 (Stateless JWT Authentication, BCrypt)
  - Spring Data JPA & Hibernate 6
  - MySQL Connector / J (with optional embedded H2 fallback for headless integration tests)
  - Jakarta Bean Validation
  - Java JWT (`io.jsonwebtoken:jjwt`)
  - Maven
- **Frontend**:
  - HTML5 & CSS3 (Custom Responsive Enterprise Theme, CSS Grid/Flexbox)
  - Vanilla JavaScript (Fetch API, Async/Await, Modular Controllers)
  - No external bulky JavaScript frameworks required.

---

## 📂 Project Structure

```text
IntelliDesk/
├── intellidesk-backend/
│   ├── src/main/java/com/intellidesk/
│   │   ├── config/              # Security, CORS, DataInitializer
│   │   ├── controller/          # REST Controllers
│   │   ├── dto/                 # Request & Response Transfer Objects
│   │   ├── entity/              # JPA Entities (User, Ticket, Comment, etc.)
│   │   ├── exception/           # Global Exception Handling & Error Details
│   │   ├── repository/          # Spring Data JPA Repositories
│   │   ├── security/            # JWT Token Provider, Filters, UserDetails
│   │   ├── service/             # Business Logic & Service Interfaces
│   │   │   └── impl/            # Service Implementations (AI, Classification, etc.)
│   │   └── IntellideskApplication.java
│   ├── src/main/resources/
│   │   └── application.properties
│   ├── src/test/java/com/intellidesk/  # Unit & Integration Tests
│   └── pom.xml
│
└── intellidesk-frontend/
    ├── index.html               # Landing & Route Redirector
    ├── login.html               # Authentication & Demo Fill
    ├── register.html            # New Employee Registration
    ├── dashboard.html           # Role-based KPI Dashboard
    ├── tickets.html             # Filterable & Searchable Tickets Table
    ├── create-ticket.html       # Ticket Creation with AI Suggest
    ├── ticket-details.html      # Incident Details & Comments Timeline
    ├── knowledge-base.html      # Search Solutions & Article Reader
    ├── ai-assistant.html        # Interactive AI IT Chatbot
    ├── users.html               # Admin User Management
    ├── reports.html             # Admin Analytics & KPI Charts
    ├── css/
    │   └── style.css            # Enterprise Slate & Blue Theme
    └── js/
        ├── api.js               # Unified API Client & Toasts
        ├── auth.js              # Token Storage & Role Navigation
        ├── dashboard.js         # Metrics & Recent Feeds
        ├── tickets.js           # Filtering, Pagination & Search
        ├── ticket-details.js    # Comments & Triage Actions
        ├── knowledge-base.js    # Article Search & Publishing
        ├── users.js             # User CRUD & Status Toggle
        └── reports.js           # Analytics Visualizer
```

---

## 🗄️ Database Setup

1. Open your MySQL client (MySQL Workbench, Command Line, or phpMyAdmin) and create the database:

```sql
CREATE DATABASE root12 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

*(Note: The connection string includes `createDatabaseIfNotExist=true`, which will also create the database automatically on startup if your user has permission).*

2. Review `intellidesk-backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/root12?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root24
```

> **Configuring via Environment Variables (Optional):**
> You can override the credentials without editing the file by setting environment variables:
> - `SPRING_DATASOURCE_URL`
> - `SPRING_DATASOURCE_USERNAME`
> - `SPRING_DATASOURCE_PASSWORD`

Hibernate automatically creates all tables and relational foreign keys on application startup (`spring.jpa.hibernate.ddl-auto=update`).

---

## 🔑 Default Development Credentials

When the database is empty, the application automatically seeds sample users, departments, categories, verified knowledge base articles, and sample tickets:

| Role | Email | Password | Access Privileges |
| :--- | :--- | :--- | :--- |
| **ADMIN** | `admin@intellidesk.com` | `admin123` | Full access: Users, Reports, Categories, Departments, Tickets, KB |
| **IT_AGENT** | `agent@intellidesk.com` | `agent123` | Ticket management, assign, status update, KB authoring |
| **IT_AGENT** | `agent2@intellidesk.com` | `agent123` | Network & hardware tier-2 support |
| **EMPLOYEE** | `employee@intellidesk.com` | `employee123` | Log tickets, view own tickets, comments, AI assistant, KB |
| **EMPLOYEE** | `sarah@intellidesk.com` | `employee123` | HR employee account |

*(On the login page, you can click any of the "Quick Demo Credentials" buttons to auto-fill these logins.)*

---

## 🚀 Running the Application

### 1. Start the Backend
Navigate to the backend directory and run:

```bash
cd intellidesk-backend
mvn spring-boot:run
```
*(Or use the included Maven wrapper on Windows: `mvnw.cmd spring-boot:run`)*

The Spring Boot backend will start on **http://localhost:8080**.

### 2. Launch the Frontend
You can open `intellidesk-frontend/index.html` directly in your browser, or serve it using any local static web server:

**Option A (VS Code Live Server):**
Right-click `intellidesk-frontend/index.html` and click **"Open with Live Server"**.

**Option B (Python Simple HTTP Server):**
```bash
cd intellidesk-frontend
python -m http.server 5500
```
Then navigate to **http://localhost:5500**.

**Option C (Node.js `serve` / `npx`):**
```bash
npx serve intellidesk-frontend
```

---

## 📡 REST API Reference

### Authentication
- `POST /api/auth/register` — Register a new employee
- `POST /api/auth/login` — Authenticate and obtain JWT Bearer token
- `GET /api/auth/me` — Retrieve current authenticated user profile

### Tickets
- `GET /api/tickets` — List tickets with search, filtering, and pagination
- `GET /api/tickets/my` — Retrieve current user's tickets
- `GET /api/tickets/assigned` — Retrieve tickets assigned to logged-in agent
- `GET /api/tickets/{id}` — Get ticket details by ID
- `GET /api/tickets/number/{ticketNumber}` — Get ticket by number (e.g. `INC-10001`)
- `POST /api/tickets` — Submit a new ticket
- `PUT /api/tickets/{id}` — Update ticket details
- `PUT /api/tickets/{id}/status` — Update ticket status (`OPEN`, `IN_PROGRESS`, `PENDING`, `RESOLVED`, `CLOSED`)
- `PUT /api/tickets/{id}/priority` — Update priority (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`)
- `PUT /api/tickets/{id}/assign` — Assign ticket to an IT Agent
- `PUT /api/tickets/{id}/resolve` — Mark ticket resolved
- `PUT /api/tickets/{id}/close` — Mark ticket closed
- `DELETE /api/tickets/{id}` — Delete ticket (Admin only)

### Comments
- `GET /api/tickets/{ticketId}/comments` — Retrieve all comments for a ticket
- `POST /api/tickets/{ticketId}/comments` — Add a new comment

### AI & Smart Classification
- `POST /api/ai/ask` — Ask conversational IT Copilot (searches KB + context diagnostic engine)
- `POST /api/ai/classify-ticket` — Analyzes ticket title & description to predict Category & Priority

### Knowledge Base
- `GET /api/knowledge` — List all published articles
- `GET /api/knowledge/search?keyword=vpn` — Search articles by keyword
- `GET /api/knowledge/{id}` — Get full article content
- `POST /api/knowledge` — Create a new article (Agent / Admin)
- `PUT /api/knowledge/{id}` — Update an existing article (Agent / Admin)
- `DELETE /api/knowledge/{id}` — Delete article (Agent / Admin)

### User Management (Admin)
- `GET /api/users` — Paginated user search and listing
- `GET /api/users/{id}` — Get user details
- `GET /api/users/agents` — List all active IT agents (for assignment dropdowns)
- `POST /api/users` — Create user
- `PUT /api/users/{id}` — Update user
- `PATCH /api/users/{id}/toggle-status` — Activate / Deactivate user
- `DELETE /api/users/{id}` — Delete user

### Categories & Departments
- `GET /api/categories`, `POST /api/categories`, `PUT /api/categories/{id}`, `DELETE /api/categories/{id}`
- `GET /api/departments`, `POST /api/departments`, `PUT /api/departments/{id}`, `DELETE /api/departments/{id}`

### Analytics & Reports (Admin)
- `GET /api/dashboard/summary` — Role-specific KPI metrics and recent tickets
- `GET /api/reports/summary` — Aggregated incident metrics & resolution times
- `GET /api/reports/by-category` — Incident volume by category
- `GET /api/reports/by-priority` — Incident volume by priority
- `GET /api/reports/by-status` — Incident volume by status
- `GET /api/reports/by-month` — Monthly ticket creation trends

---

## 🧪 Testing

Automated integration and unit tests are included in `intellidesk-backend/src/test/`:
- **Authentication**: Valid login, bad credentials rejection, new employee registration
- **Tickets**: Ticket creation, retrieval, status transition, sequential numbering
- **Authorization**: Verification that employees cannot access admin endpoints (`403 Forbidden`)
- **Validation**: Empty title, invalid email format, missing fields
- **AI Classification**: Heuristic verification for Network and Critical incident detection

Run tests using:
```bash
cd intellidesk-backend
mvn test
```

---

## 📄 License
This project is developed for enterprise IT Service Management demonstration and internal help-desk operations.
