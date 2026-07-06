# DevSync AI

**DevSync AI** is an AI-powered Agile project management and collaboration platform tailored for small-to-medium software teams. It provides a focused, scalable platform with AI automation reducing manual overhead in reporting, documentation, and estimation.

This project is currently in active development, transitioning from a monolith to a microservices architecture.

## 🚀 Features

- **Authentication & Authorization**: JWT-based stateless authentication with role-based access control (RBAC).
- **Organization & Team Management**: Multi-tenant architecture supporting multiple organizations, teams, and hierarchical roles.
- **Agile Project Management**: Full lifecycle management of Projects, Sprints, and Tasks (with subtasks and dependencies).
- **Collaboration**: Threaded comments and file attachments on tasks.
- **AI Assistant**: Automated sprint summaries, task prioritization, point estimation, bug explanations, and project health scoring.
- **Dashboard & Analytics**: Real-time burndown charts, velocity tracking, and workload distribution.
- **Audit & Activity Logs**: Comprehensive tracking of security events and entity state changes.

## 🏗️ Architecture Roadmap

The project is built following an evolutionary architecture approach:

### Phase 1: Monolith (Current State)
- **Tech Stack**: Java 21, Spring Boot 3.3.x, Spring Data JPA, Spring Security, PostgreSQL, Redis.
- **Structure**: Single Spring Boot application with modular packages (Auth, Org, Team, Project, Task, Comment).

### Phase 2: Microservices (Target State)
- **Services**: 
  - Auth Service
  - Org/User Service
  - Project Service
  - Task/Sprint Service
  - Notification Service
  - AI Service
- **Infrastructure**: Spring Cloud Gateway, Eureka Service Discovery, Spring Cloud Config.
- **Messaging**: RabbitMQ for asynchronous event-driven communication.

## 📂 Project Structure

```text
devsync-ai/
├── devsync-common/       # Shared DTOs, utilities, exceptions, and base entities
├── devsync-monolith/     # Core application containing all feature modules (Phase 1)
│   ├── auth/             # JWT auth, user registration, role management
│   ├── org/              # Multi-tenant organization management
│   ├── project/          # Projects and project members
│   ├── sprint/           # Sprints and sprint lifecycle
│   ├── task/             # Tasks, dependencies, and subtasks
│   ├── team/             # Teams and team members
│   └── config/           # Security, Redis, Swagger, Global Exception Handling
└── pom.xml               # Parent Maven POM
```

## 🛠️ Technology Stack

- **Backend**: Java 21, Spring Boot 3.3, Maven
- **Database**: PostgreSQL 16, Hibernate / Spring Data JPA
- **Caching**: Redis (Token blacklisting, dashboard caching, AI response caching)
- **Security**: Spring Security, JWT (jjwt), BCrypt
- **AI Integration**: Spring AI (OpenAI / Groq LLM integration)
- **API Documentation**: OpenAPI / Swagger UI
- **Containerization**: Docker, Docker Compose

## 🚀 Getting Started

### Prerequisites
- Java 21+
- Maven 3.8+
- Docker & Docker Compose (for PostgreSQL and Redis)

### Running Locally
1. Start the infrastructure services (PostgreSQL & Redis):
   ```bash
   # (Docker compose file coming soon)
   ```
2. Build the project:
   ```bash
   mvn clean install
   ```
3. Run the Monolith application:
   ```bash
   cd devsync-monolith
   mvn spring-boot:run
   ```
4. Access the API documentation (Swagger UI):
   - `http://localhost:8080/swagger-ui.html`

## 📝 License
This project is for educational and portfolio purposes.
