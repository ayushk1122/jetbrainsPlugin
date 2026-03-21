from fastapi import Body, FastAPI

from app.config import LlmSettings
from app.llm import LlmClient, MockLlmClient, OpenAiLlmClient
from app.models import SqlGenerationRequest, SqlGenerationResponse
from app.services import SqlGenerationService


app = FastAPI(title="Schema NL2SQL Service", version="0.1.0")
def _build_llm_client(current_settings: LlmSettings) -> LlmClient:
    if current_settings.provider == "openai" and current_settings.api_key:
        return OpenAiLlmClient(settings=current_settings)
    return MockLlmClient(model_name=current_settings.model)


settings = LlmSettings.from_env()
service = SqlGenerationService(llm_client=_build_llm_client(settings), settings=settings)


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}


@app.post("/generate", response_model=SqlGenerationResponse)
def generate_sql(payload: SqlGenerationRequest = Body(...)) -> SqlGenerationResponse:
    return service.generate(payload)
