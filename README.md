# DevTrace — Software Engineering Intelligence Platform

DevTrace is a software engineering intelligence platform that connects to GitHub repositories and transforms engineering activity into actionable development insights, process mining, and workflow intelligence.

---

## 🎯 Product Vision & Core Philosophy

Traditional developer analytics often reduce engineering to vanity metrics like raw commit counts or line volume. **DevTrace is built on the fundamental principle that commit counts do not measure developer productivity.** Instead, DevTrace focuses on revealing:
- **Workflow Bottlenecks:** Where PRs wait for initial review, change approvals, or merge gates.
- **Codebase Hotspots:** File- and module-level churn, modification frequency, and defect association.
- **Collaboration Flow:** Interactive contributor networks, betweenness centrality, and coordination dependencies.
- **Issue NLP Themes:** Categorization (Bug, Feature, Security, Performance) and recurring themes over time.
- **Anomaly Detection:** Rolling baselines and Isolation Forest signals across velocity and churn.
- **Explainable PR Risk:** Heuristic and machine-learning risk estimation with clear contributing factors and data limitations.

---

## 🏗️ Architecture

```
GitHub API (Commits, PRs, Reviews, Issues, Releases)
        │
        ▼
Spring Boot Ingestion Service (Pagination, Rate Limits, Checkpoints)
        │
        ▼
Synchronization Job & Apache Kafka KRaft Event Pipeline
        │
        ├── Commit Processor
        ├── Pull Request Processor
        ├── Review Event Processor
        ├── Issue Processor
        └── Release Processor
        │
        ▼
PostgreSQL Database (Flyway Migrations)
        │
        ├── Process Mining Engine (PR Lifecycle Analysis)
        ├── Code Churn & Hotspot Detector
        ├── Collaboration Network Analysis
        ├── Issue NLP Classifier
        ├── Rolling Baseline Anomaly Detector
        └── Explainable PR Risk Estimator
        │
        ▼
Spring Boot REST API (OpenAPI / Swagger 3)
        │
        ▼
React + TypeScript + Tailwind CSS Intelligence Dashboard
```

---

## 🚀 Technology Stack

- **Backend:** Java 21, Spring Boot 3.3.5, Spring Web, Spring Data JPA, Spring Security, Spring Kafka, Actuator, Flyway.
- **Database:** PostgreSQL 16.
- **Messaging:** Apache Kafka (KRaft mode).
- **Frontend:** React 18, TypeScript, Vite, Tailwind CSS, Recharts, Lucide Icons.
- **Data Science / Supporting:** Python (pandas, scikit-learn, networkx) for advanced ML/NLP workers.
- **Infrastructure:** Docker & Docker Compose.

---

## 🛠️ Quick Start

### 1. Prerequisites
- Java 21 LTS
- Maven 3.9+ (or use the included `./mvnw`)
- Node.js 20+ & npm
- Docker & Docker Compose

### 2. Environment Configuration
Copy `.env.example` to `.env`:
```bash
cp .env.example .env
```

### 3. Run with Docker Compose
```bash
docker-compose up -d
```
- PostgreSQL: `localhost:5432`
- Kafka KRaft: `localhost:9092`
- DevTrace Backend: `http://localhost:8080`
- DevTrace Dashboard: `http://localhost:5173`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

### 4. Run Locally for Development

**Backend:**
```bash
cd backend
./mvnw spring-boot:run
```

**Frontend:**
```bash
cd frontend
npm install
npm run dev
```

---

## 📋 Phase Roadmap
- [x] **Phase 01:** Establish Project Foundation (Architecture, Spring Boot, React, Docker Compose, Health APIs)
- [ ] **Phase 02:** Database and Domain Model (Flyway, JPA entities, repositories)
- [ ] **Phase 03:** GitHub Integration (API client, rate limiting, incremental checkpoints)
- [ ] **Phase 04:** Asynchronous Ingestion (Kafka producers/consumers, idempotent jobs)
- [ ] **Phase 05:** Core Analytics (Overview, activity trends, cycle times)
- [ ] **Phase 06:** Development Process Mining (PR lifecycle stages, review bottlenecks)
- [ ] **Phase 07:** Code Churn and Hotspots (Modification frequency, defect association)
- [ ] **Phase 08:** Collaboration Graph (Contributor network, centrality, density)
- [ ] **Phase 09:** Issue NLP (Topic summaries, automated categorization)
- [ ] **Phase 10:** Anomaly Detection (Rolling baselines, Isolation Forest)
- [ ] **Phase 11:** PR Risk Estimation (Explainable risk scoring, predictive model)
- [ ] **Phase 12:** Dependency Graph (Module relationships, architecture churn)
- [ ] **Phase 13:** Frontend Integration (End-to-end dashboard visualization)
- [ ] **Phase 14:** Integration and Security (Hardening, auth, safe errors)
- [ ] **Phase 15:** Testing and CI (Automated test suites, GitHub Actions)
- [ ] **Phase 16:** Deployment and Documentation (Production readiness)
