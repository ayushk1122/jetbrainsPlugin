from __future__ import annotations

import json
from dataclasses import dataclass, field
from typing import Any
from urllib import error, request
from typing import Protocol

from app.config import LlmSettings
from app.models import DatabaseSchema


@dataclass(frozen=True)
class LlmResult:
    sql: str | None
    warnings: list[str] = field(default_factory=list)
    clarifying_questions: list[str] = field(default_factory=list)
    guidance: list[str] = field(default_factory=list)


class LlmClient(Protocol):
    def generate_sql(self, prompt: str, schema: DatabaseSchema, user_prompt: str) -> LlmResult:
        ...


class MockLlmClient:
    def __init__(self, model_name: str) -> None:
        self._model_name = model_name

    @property
    def model_name(self) -> str:
        return self._model_name

    def generate_sql(self, prompt: str, schema: DatabaseSchema, user_prompt: str) -> LlmResult:
        first_table = schema.tables[0] if schema.tables else None
        if first_table is None:
            return LlmResult(
                sql=None,
                warnings=["No tables were provided in the schema snapshot."],
            )

        qualified_name = ".".join(part for part in [first_table.schema_name, first_table.name] if part)
        columns = ", ".join(column.name for column in first_table.columns[:5]) or "*"
        if columns == "*":
            columns = "1"
        return LlmResult(
            sql=f"SELECT {columns}\nFROM {qualified_name}\nLIMIT 25;",
            warnings=[
                "Using mock LLM client.",
                "Real model integration is not configured yet.",
            ],
        )


class OpenAiLlmClient:
    def __init__(self, settings: LlmSettings) -> None:
        if not settings.api_key:
            raise ValueError("SCHEMA_NL2SQL_LLM_API_KEY is required for provider=openai.")
        self._settings = settings

    def generate_sql(self, prompt: str, schema: DatabaseSchema, user_prompt: str) -> LlmResult:
        endpoint = (self._settings.api_base_url or "https://api.openai.com/v1").rstrip("/") + "/chat/completions"
        body = {
            "model": self._settings.model,
            "temperature": self._settings.temperature,
            "max_tokens": self._settings.max_tokens,
            "response_format": {"type": "json_object"},
            "messages": [
                {
                    "role": "system",
                    "content": (
                        "Return strict JSON with keys: sql, clarifying_questions, guidance, warnings. "
                        "If SQL should not be returned, set sql to null and provide clarifying_questions/guidance."
                    ),
                },
                {"role": "user", "content": prompt},
            ],
        }
        payload = json.dumps(body).encode("utf-8")
        req = request.Request(
            endpoint,
            data=payload,
            headers={
                "Content-Type": "application/json",
                "Authorization": f"Bearer {self._settings.api_key}",
            },
            method="POST",
        )

        try:
            with request.urlopen(req, timeout=self._settings.timeout_seconds) as response:
                raw = response.read().decode("utf-8")
        except error.HTTPError as exc:
            detail = exc.read().decode("utf-8", errors="replace")
            raise RuntimeError(f"OpenAI API request failed ({exc.code}): {detail}") from exc
        except error.URLError as exc:
            raise RuntimeError(f"OpenAI API network error: {exc.reason}") from exc

        response_json = json.loads(raw)
        content = (
            response_json.get("choices", [{}])[0]
            .get("message", {})
            .get("content", "")
        )

        if not content:
            return LlmResult(sql=None, warnings=["OpenAI returned an empty completion response."])

        parsed = _parse_llm_json_content(content)
        return LlmResult(
            sql=parsed.get("sql"),
            clarifying_questions=_as_str_list(parsed.get("clarifying_questions")),
            guidance=_as_str_list(parsed.get("guidance")),
            warnings=_as_str_list(parsed.get("warnings")),
        )


def _parse_llm_json_content(content: str) -> dict[str, Any]:
    try:
        parsed = json.loads(content)
        return parsed if isinstance(parsed, dict) else {}
    except json.JSONDecodeError:
        lowered = content.strip().lower()
        if lowered.startswith("select") or lowered.startswith("with"):
            return {"sql": content.strip(), "warnings": ["Model returned plain SQL instead of JSON object."]}
        return {"sql": None, "warnings": ["Model output was not valid JSON."]}


def _as_str_list(value: Any) -> list[str]:
    if value is None:
        return []
    if isinstance(value, str):
        return [value]
    if isinstance(value, list):
        return [item for item in value if isinstance(item, str)]
    return []
