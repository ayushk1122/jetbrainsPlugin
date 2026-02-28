# Python Service

This folder contains the future external SQL generation service for the plugin.

Current state:

- accepts the normalized schema snapshot from the Kotlin plugin
- returns placeholder SQL through a stable HTTP contract
- does not yet perform real LLM or agentic reasoning

## Intended Contract

`POST /generate`

Request body:

- `prompt`
- `dataSourceName`
- `schema`

Response body:

- `sql`
- `model`
- `warnings`
- `errors`

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
