from __future__ import annotations

import re
from dataclasses import dataclass, field


WRITE_INTENT_PATTERN = re.compile(
    r"\b(insert|update|delete|upsert|create|alter|drop|truncate|grant|revoke|replace)\b",
    re.IGNORECASE,
)

DISALLOWED_SQL_PATTERN = re.compile(
    r"\b(insert|update|delete|merge|upsert|create|alter|drop|truncate|grant|revoke|replace|copy|execute)\b",
    re.IGNORECASE,
)


@dataclass(frozen=True)
class SqlPolicyResult:
    is_valid: bool
    findings: list[str] = field(default_factory=list)
    unsupported: bool = False


def has_write_intent(user_prompt: str) -> bool:
    return bool(WRITE_INTENT_PATTERN.search(user_prompt))


def validate_read_only_sql(sql: str) -> SqlPolicyResult:
    findings: list[str] = []
    normalized = sql.strip()
    lowered = normalized.lower()

    if not normalized:
        findings.append("Generated SQL is empty.")
        return SqlPolicyResult(is_valid=False, findings=findings)

    if lowered.count(";") > 1:
        findings.append("Only a single SQL statement is allowed.")

    if not (lowered.startswith("select") or lowered.startswith("with")):
        findings.append("Only SELECT/CTE read-only SQL is supported.")

    if DISALLOWED_SQL_PATTERN.search(lowered):
        findings.append("Detected write or administrative SQL, which is not allowed in read-only mode.")
        return SqlPolicyResult(is_valid=False, findings=findings, unsupported=True)

    if re.search(r"\bselect\s+\*", lowered):
        findings.append("SELECT * is not allowed; select explicit columns.")

    if "limit" not in lowered:
        findings.append("Query should include LIMIT for safety in read-only mode.")

    return SqlPolicyResult(is_valid=not findings, findings=findings)

