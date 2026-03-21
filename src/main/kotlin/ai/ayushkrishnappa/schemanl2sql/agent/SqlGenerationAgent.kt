package ai.ayushkrishnappa.schemanl2sql.agent

import ai.ayushkrishnappa.schemanl2sql.schema.model.DatabaseSchema

interface SqlGenerationAgent {
    fun generate(request: SqlGenerationRequest): SqlGenerationResult
}

data class SqlGenerationRequest(
    val prompt: String,
    val schema: DatabaseSchema,
    val dataSourceName: String? = null,
)

data class SqlGenerationResult(
    val sql: String? = null,
    val model: String? = null,
    val warnings: List<String> = emptyList(),
    val errors: List<String> = emptyList(),
)
