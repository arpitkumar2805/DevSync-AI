# DevSync AI

**DevSync AI** is an AI-powered Agile project management and collaboration platform tailored for small-to-medium software teams. It provides a focused, scalable platform that replicates the core value proposition of enterprise tools like Jira or Azure DevOps, but with native AI automation to reduce manual overhead in reporting, documentation, and estimation by over 50%.

---

## 📑 Table of Contents

- [Executive Summary](#-executive-summary)
- [Key Features](#-key-features)
- [System Architecture](#-system-architecture)
  - [Phase 1: Monolith (Current)](#phase-1-monolith)
  - [Phase 2: Microservices (Target)](#phase-2-microservices)
- [Data Model & Entities](#-data-model--entities)
- [API Modules](#-api-modules)
- [AI Integration Design](#-ai-integration-design)
- [Technology Stack](#-technology-stack)
- [Getting Started](#-getting-started)
- [Future Enhancements](#-future-enhancements)

---

## 🎯 Executive Summary

SMB software teams often lack access to affordable, right-sized PM tooling. Enterprise tools are expensive and overly complex, while spreadsheets lack necessary structure. **DevSync AI** fills this gap by offering a scalable platform for teams of 5–200 people. 

The system is designed to evolve from a modular monolith (Java/JDBC/Spring Boot) into a fully horizontally scalable Spring Boot microservices platform, mirroring a realistic SaaS engineering lifecycle.

---

## 🚀 Key Features

1. **Authentication & Security (Auth)**: JWT-based stateless authentication (access & refresh tokens) with robust Role-Based Access Control (RBAC). Passwords are cryptographically hashed using BCrypt.
2. **Organization & Team Management**: Multi-tenant architecture supporting organization creation, member invitations, and team hierarchies.
3. **Agile Project Management**: 
   - **Projects**: Track active, on-hold, or completed projects.
   - **Sprints**: Manage sprint lifecycles (planned, active, closed) with automatic carry-over of incomplete tasks.
   - **Tasks**: Full state machine (Backlog → Todo → In Progress → Review → Done). Track dependencies, subtasks, story points, and due dates.
4. **Collaboration**: Threaded comments on tasks with mentions, and file attachment support (up to 10MB).
5. **AI Assistant**: Wraps Spring AI (Groq / OpenAI GPT-12B OSS) to generate sprint summaries, prioritize tasks, estimate story points, draft release notes, and assess project health.
6. **Dashboard & Analytics**: Redis-cached real-time metrics including burndown charts, team velocity, and workload distribution.
7. **Audit & Activity Logs**: Comprehensive, immutable timeline logs for state changes and critical security events (login, permission changes).

---

## 🏗 System Architecture

The project follows an evolutionary architecture, allowing rapid initial development that seamlessly transitions to a highly scalable enterprise architecture.

### Phase 1: Monolith (Current State)
The application starts as a single Spring Boot process to de-risk the domain model and increase early development velocity.
- **Layers**: Controller (REST) → Service (Business Logic) → Repository (Spring Data JPA)
- **Data Flow**: Controllers exchange DTOs exclusively. Entities never leave the service layer boundary.
- **Storage**: Primary data resides in PostgreSQL. Redis is used heavily alongside for dashboard caching and JWT token blacklisting.

### Phase 2: Microservices (Target State)
The monolith is structurally prepared to be split into the following services behind an **API Gateway** (Spring Cloud Gateway):
- **Auth Service**: Manages logins, JWT issuance, and password resets.
- **Org/User Service**: Handles organizations, users, roles, and teams.
- **Project Service**: Manages projects and their members.
- **Task/Sprint Service**: Manages the core Agile components (Sprints, Tasks, Comments, Attachments).
- **Notification Service**: Consumes async events and handles user notifications.
- **AI Service**: Dedicated microservice for orchestrating LLM calls via Spring AI.

**Cross-Cutting Infrastructure**:
- **Service Discovery**: Netflix Eureka
- **Centralized Config**: Spring Cloud Config
- **Async Messaging**: RabbitMQ for decoupled event-driven communication (e.g., `TaskUpdated` triggers the `Notification Service`).

---

## 🗄 Data Model & Entities

The relational data model contains 19 highly normalized entities. Soft-deletion (`deleted = false`) and Auditing (`createdAt`, `updatedAt`, `createdBy`) are enforced universally via `@MappedSuperclass`.

**Core Relationships:**
- **Organization** (1-to-N) -> Users, Teams, Projects
- **User** (N-to-N) -> Teams (via `TeamMember`), Projects (via `ProjectMember`)
- **Role** (N-to-N) -> Permissions (via `RolePermission`)
- **Project** (1-to-N) -> Sprints, Tasks
- **Task** (1-to-N) -> Comments, Attachments
- **Task** (N-to-N) -> Task Dependencies (self-referencing block/relate)

---

## 🔌 API Modules

All APIs are versioned under `/api/v1/` and documented via OpenAPI/Swagger.
- `/api/v1/auth` - Registration, login, refresh, logout
- `/api/v1/orgs` - Multi-tenant organization CRUD and invites
- `/api/v1/users` - User profiles, role assignments
- `/api/v1/teams` - Team management and lead assignments
- `/api/v1/projects` - Project settings, team assignments, transitions
- `/api/v1/sprints` - Sprint lifecycle (start/close)
- `/api/v1/tasks` - Task CRUD, state machine transitions, assignees
- `/api/v1/tasks/{id}/comments` - Threaded task discussions
- `/api/v1/dashboard` - Burndown & velocity analytics (Redis cached)
- `/api/v1/ai` - AI Assistant endpoints

---

## 🤖 AI Integration Design

The AI module uses **Spring AI** to abstract LLM interactions, allowing pluggable providers (currently configured for Groq / GPT-12B OSS).

**Workflow:**
1. **Context Assembly**: The Java service pulls relevant project/sprint data from PostgreSQL.
2. **Prompt Templating**: The data is injected into system-defined prompts.
3. **LLM Execution**: The request is sent asynchronously.
4. **Parsing**: The structured JSON response is parsed into DTOs.
5. **Caching**: AI responses are aggressively cached in Redis (keyed by feature + entity ID + data hash) to minimize API costs and latency.
6. **Logging**: Every LLM interaction is persisted to `AIRequestLog` for auditing and cost-tracking.

---

## 🛠 Technology Stack

- **Backend Language**: Java 21 (LTS)
- **Framework**: Spring Boot 3.3.x
- **Data Access**: Spring Data JPA / Hibernate
- **Database**: PostgreSQL 16
- **Caching & Rate Limiting**: Redis 7
- **Security**: Spring Security, jjwt (JSON Web Tokens)
- **AI Framework**: Spring AI
- **API Documentation**: SpringDoc OpenAPI (Swagger 3)
- **Containerization**: Docker & Docker Compose
- **Testing**: JUnit 5, Mockito, Testcontainers

---

## 🚀 Getting Started

### Prerequisites
- **Java 21+** installed
- **Maven 3.8+** installed
- **Docker & Docker Compose** (for running PostgreSQL and Redis locally)
- A Groq API Key (for AI features)

### 1. Start Infrastructure Services
Start the PostgreSQL and Redis containers using Docker Compose (compose file located in root):
```bash
docker-compose up -d postgres redis
```

### 2. Environment Variables
Create an `application-local.yml` or export the following variables:
```bash
export JWT_SECRET="your-256-bit-secure-secret-key-goes-here"
export GROQ_API_KEY="your-groq-api-key"
```

### 3. Build the Application
Navigate to the root directory and build the multi-module Maven project:
```bash
mvn clean install
```

### 4. Run the Monolith
Navigate to the monolith application module and start the Spring Boot server:
```bash
cd devsync-monolith
mvn spring-boot:run
```

### 5. Access the API
- The server runs on `http://localhost:8080`
- Access the Swagger UI documentation at: `http://localhost:8080/swagger-ui.html`

---

## 🔮 Future Enhancements

- **Microservices Split**: Execute the architectural transition to API Gateway, Eureka, and individual bounded-context services.
- **RAG Chat Assistant**: Integration with PGVector to allow users to chat with their project documentation and codebase.
- **WebSocket Sync**: Real-time push updates for Kanban board synchronization.
- **Slack/Teams Integration**: Webhook triggers for immediate task updates and sprint notifications.

---
*DevSync AI — Built for the modern Agile workflow.*
