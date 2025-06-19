
# 📝 Task Management Backend

A Spring Boot 3.5.0 application for managing tasks with robust backend capabilities including JWT-based authentication, role-based authorization, task scheduling, Kafka integration, Redis-based locking, health monitoring, and developer-friendly tooling.

---

## 🔐 Features

### ✅ Authentication & Authorization
- JWT-based login with Spring Security 6
- Role-based access (`ROLE_USER`, `ROLE_ADMIN`)
- Secure endpoints for task operations

### 📋 Task Management
- Task CRUD: Create, Read, Update, Delete
- Tasks include: `title`, `description`, `dueDate`, `flag` (completed status), `userId`, etc.
- Toggle completion using `Flag/Unflag` button
- Inline task view with "Delete" and "Flag/Unflag"
- Only update task status via flag APIs using DTOs

### 🕒 Scheduled Task Closure
- Automatically close tasks past their due date
- Distributed locking via Redis + ShedLock
- System-aware logging for each scheduler execution
- Scheduler logs stored in DB (`SchedulerLog` entity)

### 🔁 Kafka + Avro Integration
- Kafka producer for task events
- Avro serialization and Schema Registry support
- Sample task events ingestion via `data.json` (non-array format)

### 🧪 Developer Tools & Observability
- Swagger UI + JWT Auth Support
- Spring Boot Actuator endpoints (`/actuator/health`)
- Pretty JSON logs (Logback + Logstash encoder)
- SonarQube analysis
- JaCoCo test coverage reports

---

## ⚙️ Tech Stack

- Java 21
- Spring Boot 3.5.x
- Spring Security 6
- Spring Scheduler + Redis + ShedLock
- Spring Data JPA (MySQL)
- Kafka, Avro, Schema Registry
- Docker Compose
- Swagger/OpenAPI
- JWT (JJWT)
- Lombok
- SonarQube
- Jacoco

---

## 🚀 Getting Started

### 1. Clone the project

```bash
git clone https://github.com/yourusername/task-management-backend.git
cd task-management-backend
```

### 2. Setup MySQL

Create DB:
```sql
CREATE DATABASE task_db;
```

### 3. Docker Compose Setup

Ensure Docker is installed and run:

```bash
docker compose up -d
```

This brings up:
- Zookeeper
- Kafka
- Schema Registry
- Redis
- MySQL

---

## ✅ Current Functional Summary

| Feature                       | Status   |
|------------------------------|----------|
| JWT Login + Role-Based Auth  | ✅ Done  |
| CRUD Task APIs               | ✅ Done  |
| Flag/Unflag (completion)     | ✅ Done  |
| Inline task view with actions| ✅ Done  |
| Redis + ShedLock Scheduler   | ✅ Done  |
| Kafka Avro Event Producer    | ✅ Done  |
| Swagger UI + JWT Support     | ✅ Done  |
| Docker Compose Services      | ✅ Done  |
| Actuator Health Checks       | ✅ Done  |
| Sonar + JaCoCo Integration   | ✅ Done  |

---

## 🧠 Future Enhancements

- ✅ Pagination & filtering in task APIs
- ✅ Kafka consumer for task events
- ✅ Admin dashboard APIs
- ✅ Task priority, labels, and status
- ✅ Comment support per task
- ✅ Audit logs and changelogs
- ✅ Prometheus + Grafana metrics
- ✅ Dockerize backend with Dockerfile
- ✅ CI/CD pipeline with GitHub Actions
- ✅ Email reminders or Slack alerts for due tasks

---