# FailureIQ

FailureIQ is a local-first QA intelligence demo for automated test results.

It combines:

- a Spring Boot backend with PostgreSQL
- a React dashboard for investigation
- a fake web app for Selenium/TestNG tests to exercise
- a Selenium/TestNG automation framework that uploads results automatically after each run
- rule-based analysis and optional AI-assisted summaries and triage guidance

The project is intentionally beginner-friendly. It is designed to show how a test reporting platform can evolve from simple run storage into a richer triage workflow.

## What FailureIQ Does Today

FailureIQ already supports:

- storing test runs and individual test case results
- viewing recent runs and run detail pages
- historical trends across runs
- flaky test detection
- recurring failure detection
- run-to-run diff at the individual test level
- rule-based failure clustering
- per-test history across recent runs
- screenshot quick view for failed tests
- AI or deterministic run summaries
- AI or deterministic triage assistant guidance

## Repository Structure

```text
test-automation-dashboard/
|- backend/
|  |- Spring Boot API, analysis services, AI summary and triage services
|- failureiq-dashboard/
|  |- React frontend for dashboard, runs, summaries, clusters, and triage
|- fake-web-app/
|  |- Local React app used as the Selenium/TestNG target application
|- automation-framework/
|  |- Selenium/TestNG suite with profile-driven failure patterns and auto-upload
|- sample-data/
|  |- Optional legacy sample JSON files
|- scripts/
|  |- Utility scripts for sample data generation and import
|- docker-compose.yml
|- package.json
`- README.md
```

## Main Apps And Ports

- Backend API: [http://localhost:8080](http://localhost:8080)
- FailureIQ dashboard: [http://localhost:5174](http://localhost:5174)
- Fake web app: [http://localhost:5173](http://localhost:5173)

## Prerequisites

You should have these installed locally:

- Docker Desktop
- Node.js and npm
- Java 17
- Maven

This repo also includes root scripts that use the IntelliJ bundled JBR and Maven path used on this machine. If your local Java or Maven setup is different, adjust those commands as needed.

## Quick Start

### 1. Install the frontend dependencies

From the repo root:

```powershell
cd C:\Users\wolf-ai\Workspace\test-automation-dashboard
& 'C:\Program Files\nodejs\npm.cmd' install
& 'C:\Program Files\nodejs\npm.cmd' run install:frontends
```

### 2. Start PostgreSQL

```powershell
cd C:\Users\wolf-ai\Workspace\test-automation-dashboard
docker compose up -d
```

To stop it later:

```powershell
docker compose down
```

### 3. Start the backend

```powershell
cd C:\Users\wolf-ai\Workspace\test-automation-dashboard\backend
$env:JAVA_HOME = 'C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.3\jbr'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
& 'C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.3\plugins\maven\lib\maven3\bin\mvn.cmd' spring-boot:run
```

You can also use the root shortcut:

```powershell
cd C:\Users\wolf-ai\Workspace\test-automation-dashboard
& 'C:\Program Files\nodejs\npm.cmd' run backend
```

### 4. Start the frontend apps

Run both frontend apps together:

```powershell
cd C:\Users\wolf-ai\Workspace\test-automation-dashboard
& 'C:\Program Files\nodejs\npm.cmd' run frontend
```

Or start them separately:

```powershell
cd C:\Users\wolf-ai\Workspace\test-automation-dashboard\fake-web-app
& 'C:\Program Files\nodejs\npm.cmd' run dev
```

```powershell
cd C:\Users\wolf-ai\Workspace\test-automation-dashboard\failureiq-dashboard
& 'C:\Program Files\nodejs\npm.cmd' run dev
```

### 5. Run the Selenium/TestNG suite

The automation framework uploads results directly to FailureIQ after each run.

```powershell
cd C:\Users\wolf-ai\Workspace\test-automation-dashboard\automation-framework
$env:JAVA_HOME = 'C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.3\jbr'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
& 'C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.3\plugins\maven\lib\maven3\bin\mvn.cmd' test
```

Once a run uploads successfully, open:

- dashboard: [http://localhost:5174/dashboard](http://localhost:5174/dashboard)
- runs list: [http://localhost:5174/runs](http://localhost:5174/runs)

## Root Convenience Scripts

From the repo root:

- `npm run db:start` starts PostgreSQL
- `npm run db:stop` stops PostgreSQL
- `npm run backend` starts the Spring Boot API
- `npm run frontend` starts both Vite frontends
- `npm run all` starts the backend and both frontends together

## Automation Profiles

The Selenium/TestNG suite includes repeatable profile-driven failure patterns so you can generate different FailureIQ dashboards and histories.

Current supported profiles:

- `release-candidate`
- `timing-stress`
- `ui-regression`

The default profile is set in:

`automation-framework/src/test/resources/config.properties`

You can also change the profile in:

`automation-framework/testng.xml`

Or override it from Maven:

```powershell
& 'C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.3\plugins\maven\lib\maven3\bin\mvn.cmd' -Dscenario.profile=timing-stress test
```

## Current Investigation Features

### Dashboard and run analysis

- summary metrics
- trend charts
- run comparison summary
- latest run diff
- latest run summary
- latest run AI triage preview

### Run detail workflow

- failed tests section
- screenshot quick view
- failure clusters
- cluster-to-history drilldown
- per-test history links
- executive and triage summaries
- AI triage assistant panel

### Historical intelligence

- flaky score tracking
- recurring failures
- per-test result history
- cluster history
- run-to-run diff buckets such as newly failing, fixed, still failing, added, and removed

## AI Support

FailureIQ can run entirely without AI.

If AI is disabled or unavailable, the backend still returns deterministic fallback summaries and triage guidance.

Supported provider modes:

- `fallback`
- `ollama`
- `groq`
- `openai-compatible`

AI configuration lives in:

`backend/src/main/resources/application.properties`

There are also example profile-specific configs in:

- `backend/src/main/resources/application-ollama.properties`
- `backend/src/main/resources/application-groq.properties`
- `backend/src/main/resources/application-openai-compatible.properties`

Recommended configuration pattern:

- keep `failureiq.ai.enabled=false` if you only want deterministic summaries
- use `provider=ollama` for local models
- use `provider=groq` or `provider=openai-compatible` for hosted providers
- prefer environment variables or local overrides for API keys instead of committing secrets

## API Overview

FailureIQ exposes APIs for:

- health checks
- test run ingestion
- dashboard summaries and trends
- flaky test and recurring failure analysis
- run diff
- failure clustering
- per-test history
- screenshot metadata and image serving
- AI summaries
- AI triage assistant results

Helpful endpoints to check manually:

- `GET /api/health`
- `GET /api/test-runs`
- `GET /api/test-runs/{id}`
- `GET /api/test-runs/{id}/summary`
- `POST /api/test-runs/{id}/summary/regenerate`
- `GET /api/test-runs/{id}/triage-assistant`
- `POST /api/test-runs/{id}/triage-assistant/regenerate`
- `GET /api/dashboard/summary`
- `GET /api/dashboard/latest-run-summary`
- `GET /api/dashboard/latest-run-summary-context`

## Optional Sample Data

The older sample-data flow is still in the repo for local experiments, but it is no longer the main path for populating FailureIQ.

The primary flow now is:

1. start the fake web app
2. run the Selenium/TestNG suite
3. let the framework upload the run automatically

The legacy sample-data files and scripts are still useful if you want quick seeded data without running the browser tests.

## Common Troubleshooting

### The dashboard loads but no runs appear

Check that:

- PostgreSQL is running
- the backend is running on `localhost:8080`
- the automation framework completed a run
- `upload.enabled=true` in the automation config

### The automation run finishes but nothing appears in FailureIQ

Check:

- `automation-framework/src/test/resources/config.properties`
- `failureiq.api.url=http://localhost:8080/api/test-runs`
- backend is running before the suite starts

### Screenshots do not appear

Check:

- the failed test actually captured a screenshot
- the screenshot file still exists locally under the automation output folder
- you are viewing a fresh run generated on the current machine

### AI always falls back

Check:

- `failureiq.ai.enabled`
- `failureiq.ai.provider`
- `failureiq.ai.model`
- `failureiq.ai.base-url` or `failureiq.ai.endpoint`
- API key or local provider availability

For Ollama specifically, make sure the local server is running and the configured model is installed.

## Development Notes

- This repo is local-development focused.
- Authentication is intentionally not part of the current app.
- Most fields added in recent phases are backward compatible, so older runs without newer metadata should still load correctly.
- The root `src/` folder is a leftover earlier frontend artifact. The active apps today are `failureiq-dashboard/` and `fake-web-app/`.

## Current Status

FailureIQ is no longer just a Phase 1 demo. It is now a multi-part local platform for:

- generating realistic automated test results
- storing and comparing run history
- clustering failures and tracking flaky behavior
- previewing failure screenshots
- summarizing runs with deterministic or AI-backed summaries
- guiding investigation with a run-scoped AI triage assistant
