package ai.ayushkrishnappa.schemanl2sql.agent

class HttpSqlGenerationAgent(
    private val serviceUrl: String,
) : SqlGenerationAgent {
    override fun generate(request: SqlGenerationRequest): SqlGenerationResult {
        return SqlGenerationResult(
            model = "http",
            errors = listOf(
                "External SQL generation is not implemented yet.",
                "Configured service URL: $serviceUrl",
            ),
        )
    }
}
