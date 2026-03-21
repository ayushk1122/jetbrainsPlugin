from __future__ import annotations

from typing import Literal

from pydantic import BaseModel, ConfigDict, Field


class ColumnSchema(BaseModel):
    name: str
    type: str
    nullable: bool


class ForeignKeySchema(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    column: str
    referenced_schema: str | None = Field(default=None, alias="referencedSchema")
    referenced_table: str = Field(alias="referencedTable")
    referenced_column: str = Field(alias="referencedColumn")


class TableSchema(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    schema_name: str | None = Field(default=None, alias="schema")
    name: str
    columns: list[ColumnSchema] = Field(default_factory=list)
    primary_key: list[str] = Field(default_factory=list, alias="primaryKey")
    foreign_keys: list[ForeignKeySchema] = Field(default_factory=list, alias="foreignKeys")


class DatabaseSchema(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    dialect: str | None = None
    default_schema: str | None = Field(default=None, alias="defaultSchema")
    tables: list[TableSchema] = Field(default_factory=list)


class SqlGenerationRequest(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    prompt: str
    data_source_name: str | None = Field(default=None, alias="dataSourceName")
    schema_snapshot: DatabaseSchema = Field(alias="schema")


class SqlGenerationResponse(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    status: Literal["success", "needs_clarification", "unsupported", "error"] = "error"
    sql: str | None = None
    model: str | None = None
    clarifying_questions: list[str] = Field(default_factory=list, alias="clarifyingQuestions")
    guidance: list[str] = Field(default_factory=list)
    validation_findings: list[str] = Field(default_factory=list, alias="validationFindings")
    warnings: list[str] = Field(default_factory=list)
    errors: list[str] = Field(default_factory=list)
