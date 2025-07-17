# 📝 Task Management Backend

A **Spring Boot 3.5.0** application for managing tasks with production-ready features including **JWT-based authentication**, **email verification**, **admin panel**, **task scheduling with Redis**, **Kafka-Avro event publishing**, and full observability tooling. Works with a modern React frontend and supports local deployment using **Docker Compose** and **Minikube**.

---

## 🚀 Features

- User registration and login with JWT
- Task creation, editing, deletion
- Priority and status filters
- Inline task drawer with complete/flag/delete options
- Admin panel with user/task management
- Email notifications
- Scheduled jobs using ShedLock
- Profile update and dark mode
- Full CI/CD pipeline using Jenkins
- Application metrics and logs in Grafana

---

## ✅ Prerequisites

### 🛠️ Core Tools & Runtimes
- Java 17+
- Maven
- Docker
- kubectl
- Minikube

### 🧰 Developer Utilities
- Jenkins (installed via Homebrew)
- SonarQube (latest version with a configured webhook)
- ngrok (for exposing local Jenkins/Sonar services)
- Postman or similar API testing tool (optional)

### 🛢️ Infrastructure & Services
- MySQL (remote/local – allow Mac IP access)
- Redis (if using scheduling or caching)
- Elasticsearch (port-forwarded to `9200`)
- Logstash (deployed in Minikube)
- Prometheus (running locally on port `9092`)
- Grafana (installed via Homebrew with custom config path)

### 📦 Observability Dependencies
- Spring Boot Actuator (already included)
- Prometheus config file for scraping actuator metrics
- Grafana dashboards for:
    - Metrics (via Prometheus)
    - Logs (via Elasticsearch)


### 🔧 Setup

1. **Update `application.yml`**  
   Set your **Mac's IP address** in the MySQL `url`:
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://<YOUR_MAC_IP>:3306/task_db
   ```

2. **Run Spring Boot App**
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Port Forward Backend**
   ```bash
   kubectl port-forward deployment/task-management-backend-deployment 8080:8080
   ```
---

## 📈 Observability Setup

### ✅ Prometheus (on Mac)

Start with:
```bash
prometheus --web.listen-address=":9092"
```

Prometheus scrapes metrics from:
- Spring Boot (via `/actuator/prometheus`)
- Node exporter (if enabled)
- Optional: [Node Exporter](https://prometheus.io/docs/guides/node-exporter/) for system-level metrics.


Update `prometheus.yml` targets accordingly.

---

### ✅ Grafana (on Mac)

Start with:
```bash
grafana-server --homepath /opt/homebrew/opt/grafana/share/grafana --config ~/grafana-config/grafana.ini
```

- Add Prometheus as **data source** (`http://localhost:9092`)
- Add Elasticsearch as another source for **logs**
- Create dashboards for:
    - Metrics: JVM memory, HTTP requests
    - Logs: from Elasticsearch
    - Task metrics (e.g., completed/active counts)

---

## 📦 Elasticsearch + Logstash

### ✅ Port forward Elasticsearch
```bash
kubectl port-forward deployment/elasticsearch-deployment 9200:9200
```

### ✅ Logstash runs inside Minikube
Make sure Logstash is configured to listen for logs (JSON format) from the Spring Boot app.
```
<destination>logstash:5044</destination>
```
Configure your Spring Boot `logback-spring.xml` to send logs to Logstash via TCP.

---

## 📊 SonarQube

### ✅ Start SonarQube
```bash
cd ~/sonarqube-10.x.x/bin/macosx-universal-64
./sonar.sh start
```

- Configure your Spring project with `sonar-project.properties`
- Use `sonar-scanner` to send code analysis

### ✅ Ngrok for Webhooks (optional)
- Used this to create a url to add in sonarqube webhook.
```bash
ngrok http 9090
```

Use the public URL in SonarQube to configure webhook to your Jenkins.

---

## 🔄 Jenkins CI/CD Pipeline

### ✅ Start Jenkins (via Homebrew)
```bash
brew services start jenkins-lts
```

## ✅ Optional Utilities

### 🔐 Email Verification & Password Reset

- Spring Boot Mail sender configured
- Templates sent via `/verify`, `/forgot-password`, etc.

---

## ✅ Admin Panel Features

- View all users and tasks
- Assign tasks
- View stats by status, priority

---

## 📁 Important Ports Summary

| Component         | Port                       |
|------------------|----------------------------|
| Backend (Spring) | 8080                       |
| MySQL (local)    | 3306                       |
| Frontend (UI)    | 80 (container), 3000 (dev) |
| Grafana          | 3300                       |
| Prometheus       | 9092                       |
| Jenkins          | 9090                       |
| SonarQube        | 9000                       |
| Elasticsearch    | 9200                       |
| Logstash         | 5044                       |

---

## 👨‍💻 Author

Dishant Kushwaha  
© 2025

---