package ai.ayushkrishnappa.schemanl2sql.agent.protocol

import ai.ayushkrishnappa.schemanl2sql.agent.SqlGenerationRequest
import ai.ayushkrishnappa.schemanl2sql.agent.SqlGenerationResult
import ai.ayushkrishnappa.schemanl2sql.schema.model.DatabaseSchema
import kotlinx.serialization.Serializable

@Serializable
data class SqlGenerationHttpRequest(
    val prompt: String,
    val dataSourceName: String? = null,
    val schema: DatabaseSchema,
)

@Serializable
data class SqlGenerationHttpResponse(
    val sql: String? = null,
    val model: String? = null,
    val warnings: List<String> = emptyList(),
    val errors: List<String> = emptyList(),
)

fun SqlGenerationRequest.toHttpRequest(): SqlGenerationHttpRequest {
    return SqlGenerationHttpRequest(
        prompt = prompt,
        dataSourceName = dataSourceName,
        schema = schema,
    )
}

fun SqlGenerationHttpResponse.toGenerationResult(): SqlGenerationResult {
    return SqlGenerationResult(
        sql = sql,
        model = model,
        warnings = warnings,
        errors = errors,
    )
}
