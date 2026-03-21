from __future__ import annotations

from app.models import DatabaseSchema


def build_sql_generation_prompt(user_prompt: str, schema: DatabaseSchema) -> str:
    schema_lines: list[str] = []
    default_schema = schema.default_schema or "public"

    for table in schema.tables:
        schema_name = table.schema_name or default_schema
        table_name = f"{schema_name}.{table.name}"
        column_descriptions = ", ".join(f"{col.name} {col.type}" for col in table.columns) or "(no columns)"
        schema_lines.append(f"- {table_name}: {column_descriptions}")

    schema_block = "\n".join(schema_lines) or "- (no tables)"

    return (
        "You are a PostgreSQL SQL generator for a schema-aware assistant.\n"
        "Rules:\n"
        "1) Output exactly one read-only query.\n"
        "2) Use only tables/columns from the provided schema.\n"
        "3) Prefer explicit columns; avoid SELECT *.\n"
        "4) Add LIMIT for broad list queries.\n"
        "5) If the request is ambiguous, unsafe, or impossible, return no SQL.\n\n"
        f"Schema:\n{schema_block}\n\n"
        f"User request:\n{user_prompt}\n"
    )

