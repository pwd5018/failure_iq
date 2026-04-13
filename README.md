# FailureIQ Phase 1

FailureIQ is a beginner-friendly demo project for collecting, storing, and viewing automated test results.

In Phase 1, the goal is simple:

- store fake Selenium/TestNG-style test runs in PostgreSQL
- expose backend API endpoints with Spring Boot
- generate sample JSON test result files
- import those files into the backend
- display the results in a React frontend dashboard

This is not a production system yet. It is a learning-friendly foundation for future work.

---

## What FailureIQ Is

FailureIQ is a small test reporting app.

It helps you:

- store test runs
- view summary metrics
- inspect passed, failed, and skipped tests
- review failure messages from fake automation runs

Right now, it uses generated sample data to simulate real test automation output.

---

## What Phase 1 Includes

Phase 1 includes:

- a PostgreSQL database running with Docker Compose
- a Spring Boot backend
- REST API endpoints for test runs and dashboard summary
- a fake test result generator
- a sample data importer
- a React frontend dashboard
- a test runs list page
- a run detail page
- loading and error states in the frontend
- a simple pass/fail trend chart
- highlighted failed tests for easy review

---

## Project Structure

```text
test-automation-dashboard/
├─ backend/
│  ├─ pom.xml
│  └─ src/
│     └─ main/
│        ├─ java/com/failureiq/backend/
│        │  ├─ controller/
│        │  ├─ dto/
│        │  ├─ entity/
│        │  ├─ enums/
│        │  ├─ exception/
│        │  ├─ repository/
│        │  ├─ service/
│        │  └─ FailureIqBackendApplication.java
│        └─ resources/
│           └─ application.properties
├─ sample-data/
│  ├─ run-001-nightly-smoke.json
│  ├─ run-002-regression.json
│  ├─ run-003-hotfix-validation.json
│  ├─ run-004-cross-browser.json
│  └─ run-005-release-candidate.json
├─ scripts/
│  ├─ generate-sample-data.ps1
│  └─ import-sample-data.ps1
├─ src/
│  ├─ components/
│  ├─ pages/
│  ├─ utils/
│  ├─ App.jsx
│  ├─ main.jsx
│  └─ styles.css
├─ docker-compose.yml
├─ package.json
├─ vite.config.js
└─ README.md
```

---

## Prerequisites

Before running FailureIQ, make sure you have these installed:

- Docker Desktop
- Node.js
- IntelliJ IDEA or another Java 17 setup
- Java 17
- Maven

This project was run on Windows with:

- Docker Desktop for PostgreSQL
- Node.js for the frontend
- IntelliJ bundled JBR and bundled Maven for the backend

If `java` or `mvn` are not available globally on your machine, you can still run the backend using the IntelliJ paths shown below.

---

## How To Start PostgreSQL With Docker

Open PowerShell and run:

```powershell
cd C:\Users\wolf-ai\Workspace\test-automation-dashboard
docker compose up -d
```

This starts PostgreSQL in the background.

To check if it is running:

```powershell
docker compose ps
```

To stop PostgreSQL later:

```powershell
docker compose down
```

---

## How To Run The Backend

Open a PowerShell terminal and run:

```powershell
cd C:\Users\wolf-ai\Workspace\test-automation-dashboard\backend
$env:JAVA_HOME = 'C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.3\jbr'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
& 'C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.3\plugins\maven\lib\maven3\bin\mvn.cmd' spring-boot:run
```

The backend will start on:

```text
http://localhost:8080
```

You can test it in your browser or with PowerShell:

```powershell
Invoke-RestMethod http://localhost:8080/api/health
```

Expected response:

```json
{
  "status": "UP",
  "message": "FailureIQ backend is running"
}
```

---

## How To Generate Sample Data

The generator creates realistic fake Selenium/TestNG-style JSON files.

Open PowerShell and run:

```powershell
cd C:\Users\wolf-ai\Workspace\test-automation-dashboard
powershell -ExecutionPolicy Bypass -File .\scripts\generate-sample-data.ps1
```

This writes JSON files into:

```text
sample-data/
```

Example file names:

- `run-001-nightly-smoke.json`
- `run-002-regression.json`
- `run-003-hotfix-validation.json`
- `run-004-cross-browser.json`
- `run-005-release-candidate.json`

These files include realistic failure types like:

- `TimeoutException`
- `NoSuchElementException`
- `StaleElementReferenceException`
- `AssertionError`
- `ElementClickInterceptedException`

---

## How To Import Sample Data

Make sure the backend is already running on `localhost:8080`.

Then open PowerShell and run:

```powershell
cd C:\Users\wolf-ai\Workspace\test-automation-dashboard
powershell -ExecutionPolicy Bypass -File .\scripts\import-sample-data.ps1
```

This reads the JSON files from `sample-data` and posts them to:

```text
POST /api/test-runs
```

After importing, you should have data available for the dashboard and test runs pages.

---

## How To Run The Frontend

Open a new PowerShell terminal and run:

```powershell
cd C:\Users\wolf-ai\Workspace\test-automation-dashboard
& 'C:\Program Files\nodejs\npm.cmd' install
& 'C:\Program Files\nodejs\npm.cmd' run dev
```

The frontend will start on:

```text
http://localhost:5173
```

The Vite development server is configured to proxy API requests to the backend on `localhost:8080`.

---

## How To Open The Dashboard

Once everything is running:

1. Start PostgreSQL with Docker
2. Start the backend
3. Generate sample data
4. Import sample data
5. Start the frontend

Then open this URL in your browser:

[http://localhost:5173](http://localhost:5173)

Pages available in Phase 1:

- `/dashboard`
- `/runs`
- `/runs/{id}`

Main UI features:

- summary cards
- recent runs table
- pass/fail trend chart
- run list with filters
- run detail page with highlighted failed tests

---

## Recommended Run Order

If you are starting from scratch, use this order:

### 1. Start PostgreSQL

```powershell
cd C:\Users\wolf-ai\Workspace\test-automation-dashboard
docker compose up -d
```

### 2. Start the backend

```powershell
cd C:\Users\wolf-ai\Workspace\test-automation-dashboard\backend
$env:JAVA_HOME = 'C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.3\jbr'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
& 'C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.3\plugins\maven\lib\maven3\bin\mvn.cmd' spring-boot:run
```

### 3. Generate sample files

```powershell
cd C:\Users\wolf-ai\Workspace\test-automation-dashboard
powershell -ExecutionPolicy Bypass -File .\scripts\generate-sample-data.ps1
```

### 4. Import sample files

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\import-sample-data.ps1
```

### 5. Start the frontend

```powershell
& 'C:\Program Files\nodejs\npm.cmd' run dev
```

### 6. Open the app

[http://localhost:5173](http://localhost:5173)

---

## Common Troubleshooting Tips For Windows

### Docker command does not work

If `docker` is not recognized:

- make sure Docker Desktop is installed
- start Docker Desktop manually
- wait until Docker is fully running
- open a new PowerShell window and try again

### PowerShell says script execution is disabled

If you see an error about execution policy, run the scripts like this:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\generate-sample-data.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\import-sample-data.ps1
```

### `npm` is not recognized

If `npm` is not on your PATH, use the full Node.js path:

```powershell
& 'C:\Program Files\nodejs\npm.cmd' install
& 'C:\Program Files\nodejs\npm.cmd' run dev
```

### `java` is not recognized

If Java is not installed globally, use the IntelliJ bundled Java runtime:

```powershell
$env:JAVA_HOME = 'C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.3\jbr'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

### `mvn` is not recognized

If Maven is not installed globally, use IntelliJ’s bundled Maven:

```powershell
& 'C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.3\plugins\maven\lib\maven3\bin\mvn.cmd' spring-boot:run
```

### Backend cannot connect to PostgreSQL

Check these:

- Docker Desktop is running
- `docker compose up -d` completed successfully
- port `5432` is not already being used by another PostgreSQL instance
- backend `application.properties` matches the Docker database settings

Current database settings are:

- database: `failureiq`
- username: `failureiq_user`
- password: `failureiq_password`

### Frontend shows loading or API errors

Check these:

- backend is running on `http://localhost:8080`
- frontend is running on `http://localhost:5173`
- sample data has been imported
- you started the frontend with `npm run dev`, not just a static file server

### Port already in use

If port `8080`, `5173`, or `5432` is already in use:

- stop the conflicting app
- or change the port in the related config file

---

## API Endpoints In Phase 1

Backend endpoints available now:

- `GET /api/health`
- `POST /api/test-runs`
- `GET /api/test-runs`
- `GET /api/test-runs/{id}`
- `GET /api/dashboard/summary`

---

## What Is Not Included Yet

Phase 1 does not include:

- authentication or user accounts
- AI features
- failure clustering
- root cause analysis
- Selenium test execution
- TestNG execution
- CI/CD integration
- file upload UI
- charts from a charting library
- advanced filtering or search
- editing or deleting test runs
- production deployment setup
- security hardening
- automated tests for the app itself

---

## Summary

FailureIQ Phase 1 is a simple full-stack demo that lets you:

- run PostgreSQL with Docker
- run a Spring Boot backend
- generate fake test result JSON
- import that data into PostgreSQL
- view the results in a React dashboard

It is a solid starting point for future phases.
