Thank you for clarifying. Here's the **entire `README.md` file** as **one single plain text block** — no sections broken apart, no explanation between steps — just copy-paste this entire content directly into your `README.md` file:

---

```markdown
# 📝 Task Management Backend

A Spring Boot application with JWT-based authentication and role-based access control (User/Admin), designed as the backend for a full-stack Task Management app.

## 🔐 Features

- User registration & login with JWT
- Role-based access: `ROLE_USER`, `ROLE_ADMIN`
- Task CRUD for authenticated users
- Global Exception Handling with meaningful error responses
- Swagger UI with JWT Authorize button
- Spring Security 6, MySQL, Hibernate Validator

## ⚙️ Tech Stack

- Java 17+
- Spring Boot 3.x
- Spring Security 6
- MySQL
- JWT (JJWT)
- Lombok
- Hibernate Validator
- Swagger / OpenAPI

## 🚀 Getting Started

### 1. Clone the project

```bash
git clone https://github.com/yourusername/task-management-backend.git
cd task-management-backend
```

### 2. Set up MySQL database

Create a MySQL database (e.g., `taskdb`):

```sql
CREATE DATABASE taskdb;
```

### 3. Configure application.yml

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/taskdb
    username: your_mysql_username
    password: your_mysql_password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
  application:
    name: task-manager
server:
  port: 8080
```

### 4. Run the application

Using terminal:

```bash
./mvnw spring-boot:run
```

Or run `TaskManagerApplication` class from your IDE.

## 🔑 API Authentication Flow

### Register

`POST /api/v1/auth/register`

```json
{
  "username": "testuser",
  "password": "testpass",
  "role": "USER"
}
```

### Login

`POST /api/v1/auth/login`

```json
{
  "username": "testuser",
  "password": "testpass"
}
```

📥 Response includes a JWT token.

## 🧪 Swagger UI

Once the app is running, open:

```
http://localhost:8080/swagger-ui/index.html
```

- Click “Authorize” button
- Enter: `Bearer <your_jwt_token>` to test secured APIs

## 📂 Project Structure

```
com.example.taskmanager
├── auth                 → AuthController, AuthService, Login/Register DTOs
├── config               → SecurityConfig, OpenAPIConfig
├── controller           → TaskController
├── dto                  → Request/Response objects
├── entity               → User, Task
├── exception            → Custom Exceptions + Global Handler
├── repository           → UserRepository, TaskRepository
├── security             → JwtUtil, JwtFilter
├── service              → TaskService, AuthService
```

## 💻 Build & Test

Build:

```bash
./mvnw clean install
```

Run tests (if added):

```bash
./mvnw test
```

## 🔧 Future Enhancements

- Task priority and deadline support
- Task status (Pending, In Progress, Completed)
- Pagination and sorting
- React frontend

