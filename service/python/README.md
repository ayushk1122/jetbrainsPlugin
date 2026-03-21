# Python Service

This folder contains the future external SQL generation service for the plugin.

Current state:

- accepts the normalized schema snapshot from the Kotlin plugin
- returns placeholder SQL through a stable HTTP contract
- has a config + service layer scaffold for future LLM providers
- uses a mock LLM client by default (real provider integration is next)

## Intended Contract

`POST /generate`

Request body:

- `prompt`
- `dataSourceName`
- `schema`

Response body:

- `status` (`success | needs_clarification | unsupported | error`)
- `sql`
- `model`
- `clarifyingQuestions`
- `guidance`
- `validationFindings`
- `warnings`
- `errors`

## Service Structure

- `app/config.py`: typed LLM settings loaded from environment
- `app/models.py`: request/response and schema models
- `app/services/sql_generation.py`: orchestration + error handling
- `app/llm/client.py`: LLM client contract and mock implementation
- `app/main.py`: FastAPI routes + dependency wiring

## LLM Environment Variables

- `SCHEMA_NL2SQL_LLM_PROVIDER` (default: `mock`)
- `SCHEMA_NL2SQL_LLM_MODEL` (default: `python-scaffold`)
- `SCHEMA_NL2SQL_LLM_API_KEY` (optional)
- `SCHEMA_NL2SQL_LLM_BASE_URL` (optional)
- `SCHEMA_NL2SQL_LLM_TEMPERATURE` (default: `0.0`)
- `SCHEMA_NL2SQL_LLM_MAX_TOKENS` (default: `512`)
- `SCHEMA_NL2SQL_LLM_TIMEOUT_SECONDS` (default: `30`)

To enable OpenAI via environment variables:

```powershell
$env:SCHEMA_NL2SQL_LLM_PROVIDER = "openai"
$env:SCHEMA_NL2SQL_LLM_MODEL = "gpt-4.1-mini"
$env:SCHEMA_NL2SQL_LLM_API_KEY = "<your-openai-api-key>"
```

## Current Behavior (Phase 1)

- PostgreSQL-oriented SQL generation
- read-only mode (`SELECT`/`WITH` only)
- blocks write/edit SQL operations
- enforces policy checks (single statement, explicit columns, `LIMIT`)
- returns clarifying questions when the request is ambiguous or not feasible with the schema

## Local Run

Create a virtual environment and install dependencies:

```powershell
cd service/python
python -m venv .venv
.venv\Scripts\Activate.ps1
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000
```

To point the plugin at the service, launch the sandbox IDE with:

```powershell
$env:SCHEMA_NL2SQL_AGENT_URL = "http://127.0.0.1:8000"
.\gradlew.bat runIde
```

If the env var is not set, the plugin falls back to the in-process mock agent.
