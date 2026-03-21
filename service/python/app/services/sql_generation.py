from __future__ import annotations

import re

from app.config import LlmSettings
from app.llm import LlmClient
from app.models import DatabaseSchema, SqlGenerationRequest, SqlGenerationResponse
from app.services.prompting import build_sql_generation_prompt
from app.services.sql_policy import has_write_intent, validate_read_only_sql


class SqlGenerationService:
    def __init__(self, llm_client: LlmClient, settings: LlmSettings) -> None:
        self._llm_client = llm_client
        self._settings = settings

    def generate(self, payload: SqlGenerationRequest) -> SqlGenerationResponse:
        if not payload.schema_snapshot.tables:
            return SqlGenerationResponse(
                status="unsupported",
                model=self._settings.model,
                guidance=["Provide at least one table in the schema snapshot before generating SQL."],
                errors=["No tables were provided in the schema snapshot."],
            )

        if has_write_intent(payload.prompt):
            return SqlGenerationResponse(
                status="unsupported",
                model=self._settings.model,
                guidance=[
                    "Phase 1 currently supports read-only SQL.",
                    "Rephrase the request as a SELECT query over existing schema objects.",
                ],
                errors=["Write/edit operations are not supported yet."],
            )

        feasibility = self._assess_schema_feasibility(payload.prompt, payload.schema_snapshot)
        if feasibility:
            return SqlGenerationResponse(
                status="needs_clarification",
                model=self._settings.model,
                clarifying_questions=feasibility,
                guidance=["Use table and column names that exist in the schema snapshot."],
            )

        llm_prompt = build_sql_generation_prompt(payload.prompt, payload.schema_snapshot)

        try:
            llm_result = self._llm_client.generate_sql(
                prompt=llm_prompt,
                schema=payload.schema_snapshot,
                user_prompt=payload.prompt,
            )
        except Exception as exc:
            return SqlGenerationResponse(
                status="error",
                model=self._settings.model,
                errors=[f"LLM generation failed: {exc}"],
            )

        if not llm_result.sql:
            return SqlGenerationResponse(
                status="needs_clarification",
                model=self._settings.model,
                clarifying_questions=llm_result.clarifying_questions,
                guidance=llm_result.guidance,
                warnings=llm_result.warnings,
                errors=["LLM did not return any SQL."],
            )

        policy_result = validate_read_only_sql(llm_result.sql)
        if not policy_result.is_valid:
            return SqlGenerationResponse(
                status="unsupported" if policy_result.unsupported else "needs_clarification",
                model=self._settings.model,
                sql=llm_result.sql,
                guidance=[
                    "Request a read-only PostgreSQL query that uses explicit columns and includes LIMIT."
                ],
                validation_findings=policy_result.findings,
                warnings=llm_result.warnings,
                errors=["Generated SQL did not pass service policy validation."],
            )

        return SqlGenerationResponse(
            status="success",
            sql=llm_result.sql,
            model=self._settings.model,
            warnings=llm_result.warnings,
        )

    def _assess_schema_feasibility(self, prompt: str, schema: DatabaseSchema) -> list[str]:
        prompt_lower = prompt.lower()
        table_names = [table.name.lower() for table in schema.tables]

        if self._has_ambiguous_top_request(prompt_lower):
            return [
                "How should 'top' be ranked (for example by revenue, order count, or recency)?",
                "Do you want a time window (for example last 30 days)?",
            ]

        matches = [name for name in table_names if re.search(rf"\b{re.escape(name)}\b", prompt_lower)]
        if len(schema.tables) > 1 and not matches:
            sample_tables = ", ".join(table_names[:5])
            return [f"Which table should be queried? Available tables include: {sample_tables}."]

        explicit_table_refs = re.findall(r"\btable\s+([a-zA-Z_][a-zA-Z0-9_]*)\b", prompt_lower)
        missing_tables = [name for name in explicit_table_refs if name not in table_names]
        if missing_tables:
            return [f"Table '{missing_tables[0]}' is not present in the schema. Which existing table did you mean?"]

        all_columns = {column.name.lower() for table in schema.tables for column in table.columns}
        explicit_column_refs = re.findall(r"\b(column|field)\s+([a-zA-Z_][a-zA-Z0-9_]*)\b", prompt_lower)
        missing_columns = [name for _, name in explicit_column_refs if name not in all_columns]
        if missing_columns:
            return [f"Column '{missing_columns[0]}' was not found. Can you specify a valid column name?"]

        return []

    def _has_ambiguous_top_request(self, prompt_lower: str) -> bool:
        ranking_terms = r"\b(top|highest|best|leading)\b"
        if not re.search(ranking_terms, prompt_lower):
            return False
        if re.search(r"\b(top\s+\d+)\b", prompt_lower) is None and "top" in prompt_lower:
            return False
        if re.search(r"\bby\b", prompt_lower):
            return False
        if re.search(r"\bmost\b", prompt_lower):
            return False
        return True
