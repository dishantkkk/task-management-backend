---

```markdown
# 📝 Task Management Backend

A **Spring Boot 3.5.0** application for managing tasks with production-ready features including **JWT-based authentication**, **email verification**, **admin panel**, **task scheduling with Redis**, **Kafka-Avro event publishing**, and full observability tooling. Works with a modern React frontend and supports local deployment using **Docker Compose** and **Minikube**.

---

## 🔐 Authentication & Authorization

- JWT-based login with Spring Security 6
- Role-based access (`ROLE_USER`, `ROLE_ADMIN`)
- Password strength validation during registration
- Email verification flow (with resend support)
- Forgot password and reset password functionality
- Secure endpoint access with `@PreAuthorize` checks

---

## 📋 Task Management Features

- Task CRUD: Create, Read, Update, Delete
- Fields: `title`, `description`, `dueDate`, `flag`, `priority`, `userId`
- Mark task complete/incomplete using **Flag/Unflag** API
- Task view includes delete & status toggle actions
- Filter tasks by priority/status and sort by due date
- Inline task expansion (drawer view on frontend)
- Due soon tasks highlighted visually
- Task comment support (collaboration coming soon)

---

## 🕒 Scheduled Task Closure

- Automatic scheduler to mark overdue tasks as completed
- Uses **Redis + ShedLock** for distributed locking
- Execution logs stored in DB (`SchedulerLog` entity)
- Logs include system timestamps and lock information

---

## 🧑‍💼 Admin Panel Features

- Admin dashboard with user and task metrics
- View all users with their roles
- Admin-only task insights
- Secure access with `ROLE_ADMIN`
- Future: User management, bulk actions, task reassignment

---

## 🔁 Kafka + Avro Integration

- Kafka producer to publish task-related events
- Uses **Avro serialization** and **Schema Registry**
- Sample task ingestion via `task-data.json` using `TaskEvent`
- Kafka configuration via environment variables
- Consumer integration planned

---

## 🧪 Developer Tooling & Observability

- **Swagger UI** with JWT input for testing APIs
- **Spring Boot Actuator** health endpoints
- Structured JSON logs (Logback + Logstash encoder)
- **SonarQube** integration for code quality
- **JaCoCo** for code coverage reports
- Clean DTO usage, layered architecture, and Lombok

---

## ⚙️ Tech Stack

| Component        | Tech                       |
|------------------|----------------------------|
| Language         | Java 21                    |
| Framework        | Spring Boot 3.5.x          |
| Security         | Spring Security 6, JWT     |
| DB + ORM         | MySQL, Spring Data JPA     |
| Messaging        | Apache Kafka + Avro        |
| Locking/Schedule | Redis + ShedLock Scheduler |
| DevOps           | Docker Compose, Minikube   |
| Observability    | Actuator, Logback, SonarQube, JaCoCo |
| Auth Tools       | JWT, BCrypt, Email Links   |
| API Docs         | Swagger/OpenAPI            |

---

## 🚀 Local Development Setup

### 🐳 Using Docker Compose

```bash
docker compose up -d
```

Services:
- MySQL
- Kafka
- Zookeeper
- Schema Registry
- Redis

### 🎯 Run Backend

```bash
./mvnw spring-boot:run
```

---

## ☸️ Minikube Deployment

### 1. Start Minikube

```bash
minikube start
```

### 2. Build & Push Docker Image

```bash
./deploy-backend.sh
```

This script:
- Builds and pushes Docker image to Docker Hub
- Applies K8s manifests for backend deployment
- Port-forwards backend service to `localhost:8080`

### 3. CORS Configuration

In `WebConfig.java`:

```java
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
            .allowedOrigins("http://localhost:5173", "http://localhost:3000")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
}
```

---

## 🌐 API URLs

| Endpoint Type | URL                                 |
|---------------|--------------------------------------|
| Swagger UI    | `http://localhost:8080/swagger-ui/` |
| Auth          | `/v1/api/auth/login`, `/register`, etc |
| Tasks         | `/v1/api/tasks` (CRUD)              |
| Admin APIs    | `/v1/api/admin/...`                 |
| Scheduler     | Auto-runs via ShedLock              |

---

## ✅ Functional Summary

| Feature                                 | Status   |
|----------------------------------------|----------|
| JWT Login + Role-Based Auth            | ✅ Done  |
| Email Verification + Resend Link       | ✅ Done  |
| Forgot & Reset Password Flow           | ✅ Done  |
| Task CRUD + Completion Toggle          | ✅ Done  |
| Inline Task Drawer View (Frontend)     | ✅ Done  |
| Kafka Producer + Avro Schema Registry  | ✅ Done  |
| Redis + ShedLock Task Scheduler        | ✅ Done  |
| Admin Dashboard (user/task metrics)    | ✅ Done  |
| Swagger, Actuator, Sonar, Jacoco       | ✅ Done  |
| Minikube + Docker Compose Support      | ✅ Done  |

---

## 🔮 Future Enhancements

- Pagination & filtering in task APIs
- Task labels, categories, and metadata
- Task comments and collaboration notes
- Admin user management (edit/delete roles)
- Audit logs & changelogs
- Prometheus + Grafana integration
- CI/CD pipeline using GitHub Actions
- Email/SMS alerts for due tasks

---

## 🙌 Contributions

Feel free to open issues, PRs, or suggestions!

---