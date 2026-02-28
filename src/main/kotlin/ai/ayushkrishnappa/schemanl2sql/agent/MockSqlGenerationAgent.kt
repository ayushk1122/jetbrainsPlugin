package ai.ayushkrishnappa.schemanl2sql.agent

class MockSqlGenerationAgent : SqlGenerationAgent {
    override fun generate(request: SqlGenerationRequest): SqlGenerationResult {
        return SqlGenerationResult(
            sql = """
                -- Mock SQL generation placeholder.
                -- Request: ${request.prompt}
                SELECT 1;
            """.trimIndent(),
            warnings = listOf("Using the mock SQL generation agent."),
        )
    }
}
