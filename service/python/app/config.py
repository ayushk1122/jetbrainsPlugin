from __future__ import annotations

import os
from dataclasses import dataclass


def _parse_float(value: str, fallback: float) -> float:
    try:
        return float(value)
    except (TypeError, ValueError):
        return fallback


def _parse_int(value: str, fallback: int) -> int:
    try:
        return int(value)
    except (TypeError, ValueError):
        return fallback


@dataclass(frozen=True)
class LlmSettings:
    provider: str
    model: str
    api_key: str | None
    api_base_url: str | None
    temperature: float
    max_tokens: int
    timeout_seconds: int

    @classmethod
    def from_env(cls) -> "LlmSettings":
        provider = os.getenv("SCHEMA_NL2SQL_LLM_PROVIDER", "mock").strip().lower() or "mock"
        model = os.getenv("SCHEMA_NL2SQL_LLM_MODEL", "python-scaffold").strip() or "python-scaffold"
        api_key = os.getenv("SCHEMA_NL2SQL_LLM_API_KEY")
        api_base_url = os.getenv("SCHEMA_NL2SQL_LLM_BASE_URL")
        temperature = _parse_float(os.getenv("SCHEMA_NL2SQL_LLM_TEMPERATURE", "0.0"), 0.0)
        max_tokens = _parse_int(os.getenv("SCHEMA_NL2SQL_LLM_MAX_TOKENS", "512"), 512)
        timeout_seconds = _parse_int(os.getenv("SCHEMA_NL2SQL_LLM_TIMEOUT_SECONDS", "30"), 30)

        temperature = max(0.0, min(2.0, temperature))
        max_tokens = max(64, max_tokens)
        timeout_seconds = max(5, timeout_seconds)

        return cls(
            provider=provider,
            model=model,
            api_key=api_key,
            api_base_url=api_base_url,
            temperature=temperature,
            max_tokens=max_tokens,
            timeout_seconds=timeout_seconds,
        )

