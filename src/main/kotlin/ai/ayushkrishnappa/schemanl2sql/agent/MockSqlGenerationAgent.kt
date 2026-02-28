package ai.ayushkrishnappa.schemanl2sql.agent

import ai.ayushkrishnappa.schemanl2sql.agent.mock.SchemaAwareMockSqlPlanner

class MockSqlGenerationAgent : SqlGenerationAgent {
    private val planner = SchemaAwareMockSqlPlanner()

    override fun generate(request: SqlGenerationRequest): SqlGenerationResult {
        val plan = planner.plan(request)

        return SqlGenerationResult(
            sql = plan.sql,
            model = "mock-template-planner",
            warnings = listOf(
                "Using the mock SQL generation agent.",
            ) + plan.warnings,
            errors = plan.errors,
        )
    }
}
