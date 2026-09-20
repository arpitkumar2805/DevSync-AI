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

### Phase 2: Microservices (In Progress)

The monolith has already been split into the following service modules, which live alongside it in this repo and build behind an **API Gateway** (Spring Cloud Gateway):

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

> **Note on service boundaries**: Because each microservice owns its own database, a service must never directly query another service's JPA entities/repositories. A few response fields that would normally be enriched from another service (e.g. a project member's email/name, which lives in `devsync-org-user-service`) are currently returned as `null` from `devsync-project-service` and `devsync-task-service` pending a proper inter-service client (Feign/REST call or event-driven read model). This is called out inline in the affected service classes.

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

The AI module uses **Spring AI** to abstract LLM interactions, allowing pluggable providers (currently configured for **NVIDIA NIM** — the `nvidia/llama-3.1-nemotron-70b-instruct` model — via its OpenAI-compatible `https://integrate.api.nvidia.com` endpoint). Because Spring AI's OpenAI client is provider-agnostic, switching providers again only requires changing `spring.ai.openai.base-url`, `spring.ai.openai.api-key`, and `spring.ai.openai.chat.options.model` in `application.yml` (or the `NVIDIA_API_KEY`/`AI_MODEL` env vars) — no code changes.

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

- **Java 21** installed (this project's Lombok/annotation-processor setup does not yet support Java 25/26 — `brew install openjdk@21` if your default JDK is newer, and point `JAVA_HOME` at it for builds)
- **Maven 3.8+** installed
- **Docker & Docker Compose** (for running PostgreSQL and Redis locally)
- An NVIDIA API Key (for AI features — get one at [build.nvidia.com](https://build.nvidia.com))

### 1. Start Infrastructure Services

Start the PostgreSQL and Redis containers using Docker Compose (compose file located in root):

```bash
docker-compose up -d postgres redis
```

### 2. Environment Variables

Create an `application-local.yml` or export the following variables:

```bash
export JWT_SECRET="your-256-bit-secure-secret-key-goes-here"
export NVIDIA_API_KEY="your-nvidia-api-key"
# Optional — defaults to nvidia/llama-3.1-nemotron-70b-instruct
export AI_MODEL="nvidia/llama-3.1-nemotron-70b-instruct"
```

Never commit real API keys to source control or paste them into shared chats/tickets — treat `NVIDIA_API_KEY` and `JWT_SECRET` as secrets. `devsync-ai/.env` is git-ignored and is the right place for local values; if a key is ever exposed (e.g. pasted somewhere it shouldn't have been), rotate it immediately from the provider's console.

### 3. Build the Application

Navigate to the root directory and build the multi-module Maven project (all 11 modules — common, monolith, and every microservice):

```bash
mvn clean install
```

If your machine's default `java -version` isn't 21, prefix the command, e.g.:

```bash
JAVA_HOME=$(brew --prefix openjdk@21)/libexec/openjdk.jdk/Contents/Home mvn clean install
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

## 🩹 Recent Changes (Build Health Pass)

The multi-module Maven build (`devsync-common`, `devsync-monolith`, and all 7 microservices + gateway/discovery/config-server) did not compile before this pass. The following were fixed so `mvn clean install` now succeeds end-to-end and the existing test suite passes:

- **`devsync-org-user-service` — `TeamService`**: imported `User`/`UserRepository` from a non-existent `auth` package instead of the real `user` package.
- **`devsync-project-service` — `ProjectService` & `DashboardService`**: referenced `User`/`Task` entities and repositories that belong to `devsync-org-user-service` and `devsync-task-service` respectively — code a microservice cannot compile against since it doesn't own those databases. Removed the cross-service joins; member/dashboard responses now omit the fields that would need those services (see the note under System Architecture) instead of failing to build.
- **`devsync-task-service` — `ActivityLogService`, `AttachmentService`, `CommentService`, `SprintService`, `TaskService`**: same cross-service leakage (`User` from org-user-service, `Project` from project-service). Removed the dead imports/lookups and left inline comments on the intentional gaps.
- **`devsync-api-gateway` — `JwtGatewayFilter`** and **`devsync-notification-service`, `devsync-org-user-service`, `devsync-task-service`, `devsync-project-service` — `JwtAuthenticationFilter`**: all called a `JwtUtil.validateToken(String)` method that doesn't exist on the shared `devsync-common` `JwtUtil` (the real method is `isTokenValid`), and treated `extractUserId(...)` as returning a `String` when it returns a `UUID`. Every filter now calls `isTokenValid(...)` and `.toString()`s the extracted `UUID`.
- Confirmed a clean `mvn clean install` and `mvn test` pass under **Java 21** (the machine's default JDK was 25/26, which breaks Lombok's annotation processor here — documented above).

## 🩹 Recent Changes (AI Provider Switch + Chat Hardening)

- **Switched the AI provider from Groq to NVIDIA NIM** in `devsync-monolith/src/main/resources/application.yml`: `spring.ai.openai.base-url` now points at `https://integrate.api.nvidia.com`, and the key is read from `NVIDIA_API_KEY` (was `GROQ_API_KEY`). The model is configurable via `AI_MODEL` instead of being hardcoded, currently defaulting to `nvidia/nemotron-3-super-120b-a12b`. `docker-compose.yml` and the local `.env` were updated to match.
- **Model selection required live testing, not just picking a name.** The first model tried (`nvidia/llama-3.1-nemotron-70b-instruct`) is listed in NVIDIA's public catalog but returned `404 Function ... Not found for account` for this specific API key — not every catalog model is enabled per-account/key. Querying `GET https://integrate.api.nvidia.com/v1/models` with the real key and test-calling candidates found two that actually work for this account: `nvidia/nemotron-3.5-lightning-30b-a3b` (fast, but gave incoherent output in testing) and `nvidia/nemotron-3-super-120b-a12b` (slower, coherent) — went with the latter. If you rotate to a different NVIDIA key/account, re-check `AI_MODEL` still resolves for it.
- **The chosen model is a reasoning model** — by default it returns its internal chain-of-thought as part of the answer (e.g. "We are to say hello... The shortest and most direct is..." instead of just "Hello!"). Fixed by prefixing every request with a `/no_think` system instruction in `AIService.callAiAndLog`, which reliably suppresses the reasoning preamble and returns direct answers, matching what the previous provider returned.
- **Removed dead config**: the `devsync.ai.*` (`provider: gemini`, ...) block in `application.yml` was never read by any code — `AIService` only ever used `spring.ai.openai.api-key`. Deleted it to avoid confusion about which provider is actually active.
- **Fixed the RAG Assistant Chat** (`AIService.chat`): removed a hardcoded debug bypass that special-cased the literal messages `"hi"` / `"say hello"` to skip the whole context-retrieval pipeline — a leftover test hook, not real behavior. Also removed verbose `[DEBUG STAGE n]`-style logging that printed full prompts (including task descriptions and user PII) at `INFO` level; request/response pairs are still persisted to `AIRequestLog` for auditing, but no longer echoed into application logs.
- The AI model name was previously hardcoded as the literal string `"llama-3.1-8b-instant"` in two places in `AIService`; both now read the configured `spring.ai.openai.chat.options.model` value, so the DB log (`AIRequestLog.model`) always reflects the model actually in use.
- **Known gap, not fixed in this pass**: `devsync-frontend/src/pages/ForgotPassword.tsx` only simulates success (`// Mock / placeholder response`) — the backend has no forgot-password/reset-password endpoint yet. Wiring this up for real requires a new `AuthController` endpoint (token generation + email dispatch via the existing mail config) and is a larger feature addition than an API-swap/cleanup pass; flagging it here so it isn't mistaken for working.
- **Security note**: if an API key or secret is ever pasted into a chat, ticket, or shared document, treat it as compromised and rotate it at the provider immediately, even if you also remove it from the message — this was done for the NVIDIA key introduced in this change.
- **Retry tuning**: `spring.ai.retry.max-attempts` was `1`, so a single transient `503` (NVIDIA's endpoint does occasionally report "Service temporarily overloaded") failed the whole chat request. Raised to `3`.
- **Renamed in the UI**: "RAG Assistant Chat" → "AI Chat" in `devsync-frontend/src/pages/AIAssistant.tsx`, since the feature is a context-augmented chat, not a document-embedding RAG pipeline — the old name overpromised what it does.
- **Deploying this**: the running Docker container does not pick up code or `.env` changes automatically — after any backend change, rebuild and restart with `docker compose up -d --build` from `devsync-ai/` (this is what actually fixed the live "chat shows an error" symptom: the container was still running old code against an invalid key until it was rebuilt).

## 🔐 Recent Changes (RBAC & Multi-Tenant Data Isolation)

Investigating a report that the dashboard's "Team Output Timeline" showed task activity for a week nothing had been added in surfaced two real bugs — one display bug and one genuine cross-tenant data leak — plus confirmation that role-based access control was defined but never enforced.

**What was found (in `devsync-monolith`, the service `docker-compose.yml`/`run.sh` actually run):**
- `GET /api/v1/tasks` with no filters (`TaskService.listAll`) ran `taskRepository.findAll(...)` — literally every task in the database, across **every organization**, not just the caller's. The frontend dashboard calls exactly this (no `projectId`) to build its chart, so it was rendering other tenants'/seed data alongside your own.
- `GET/PUT/DELETE /api/v1/projects/{id}` and the equivalent task endpoints did no ownership check at all — `ProjectService`/`TaskService` loaded the row by ID and returned it, with no comparison against the caller's organization. Any authenticated user who knew or guessed another org's project/task UUID could read or modify it (an IDOR).
- `SecurityConfig` only had `.anyRequest().authenticated()` — no `@PreAuthorize`/`hasRole`/`hasAuthority` existed **anywhere** in the codebase, despite `@EnableMethodSecurity` being on and every user having a `roleName` (`ORG_ADMIN`, `DEVELOPER`, ...) baked into their JWT. Roles were tracked but functionally meaningless.

**What was fixed:**
- **`ProjectService`**: added `requireOwnedProject(id)`, used by `getById`, `update`, `updateStatus`, `delete`, `addMember`, `removeMember`, and `getMembers` — every one now throws `403 Forbidden` if the project doesn't belong to the caller's organization, instead of silently serving/mutating another tenant's data.
- **`TaskService`**: added the equivalent `requireOwnedTask`/`requireOwnedProject` checks to `getById`, `update`, `updateStatus`, `assignTask`, `delete`, `create`, and `listByProject`. `listAll` (the no-filter case) no longer scans the whole table — it now resolves the caller's own project IDs (`ProjectRepository.findIdsByOrganizationId`) and queries only tasks within those projects (`TaskRepository.findByProjectIdInAndDeletedFalse`).
- **RBAC enforcement**: added `@PreAuthorize("hasRole('ORG_ADMIN')")` to `PUT /api/v1/projects/{id}`, `PUT /api/v1/projects/{id}/status`, `DELETE /api/v1/projects/{id}`, `POST /api/v1/projects/{id}/members`, and `DELETE /api/v1/projects/{id}/members/{userId}` — editing, changing status, deleting, or managing membership on a project now all require the `ORG_ADMIN` role, not just "any logged-in user in the org." Viewing and creating projects, and everything at the task level (create/edit/comment/assign), remain open to any authenticated org member. This is still a two-role model (`ORG_ADMIN` / `DEVELOPER`), not a dedicated "Manager" tier — a finer-grained role (e.g. a per-project lead who can edit only their own projects) would need new role plumbing, not just more `@PreAuthorize` checks.
- **Dashboard chart** (`devsync-frontend/src/pages/Dashboard.tsx`): "Team Output Timeline" previously bucketed tasks by weekday **name** only (`Mon`/`Tue`/...), so a task from any week in the past landed in the same bucket as one from today — it was never actually scoped to "this week." It now buckets by real calendar date across the last 7 days. Combined with the backend fix above, the data behind it is also now correctly scoped to your own organization.

**On the RBAC question specifically**: signing up does **not** make everyone an admin — the default role for a plain signup is `DEVELOPER`. You only become `ORG_ADMIN` if you create a *new* organization during registration (`AuthService.register`, standard "workspace creator is the admin" pattern) or are invited into one with that role. Before this pass, that distinction existed in name only since no endpoint checked it; a subset of destructive actions now actually enforce it (see above).

## 🔮 Future Enhancements

- **Microservices Split**: Execute the architectural transition to API Gateway, Eureka, and individual bounded-context services.
- **RAG Chat Assistant**: Integration with PGVector to allow users to chat with their project documentation and codebase.
- **WebSocket Sync**: Real-time push updates for Kanban board synchronization.
- **Slack/Teams Integration**: Webhook triggers for immediate task updates and sprint notifications.

---

_DevSync AI — Built for the modern Agile workflow._
