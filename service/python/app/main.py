from fastapi import Body, FastAPI
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
    sql: str | None = None
    model: str | None = None
    warnings: list[str] = Field(default_factory=list)
    errors: list[str] = Field(default_factory=list)


app = FastAPI(title="Schema NL2SQL Service", version="0.1.0")


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}


@app.post("/generate", response_model=SqlGenerationResponse)
def generate_sql(payload: SqlGenerationRequest = Body(...)) -> SqlGenerationResponse:
    first_table = payload.schema_snapshot.tables[0] if payload.schema_snapshot.tables else None
    qualified_name = ".".join(
        part for part in [first_table.schema_name if first_table else None, first_table.name if first_table else None] if part
    )

    if first_table is None:
        return SqlGenerationResponse(
            model="python-scaffold",
            errors=["No tables were provided in the schema snapshot."],
        )

    sql = f"SELECT *\nFROM {qualified_name}\nLIMIT 25;"

    return SqlGenerationResponse(
        sql=sql,
        model="python-scaffold",
        warnings=[
            "Using the scaffolded Python service.",
            "Real agentic SQL generation is not implemented yet.",
        ],
    )
