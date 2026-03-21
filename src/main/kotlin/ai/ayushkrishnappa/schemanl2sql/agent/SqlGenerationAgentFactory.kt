package ai.ayushkrishnappa.schemanl2sql.agent

object SqlGenerationAgentFactory {
    private const val serviceUrlProperty = "schemaNl2Sql.agentUrl"
    private const val serviceUrlEnv = "SCHEMA_NL2SQL_AGENT_URL"

    fun create(): SqlGenerationAgent {
        val serviceUrl = System.getProperty(serviceUrlProperty)
            ?.takeIf(String::isNotBlank)
            ?: System.getenv(serviceUrlEnv)?.takeIf(String::isNotBlank)

        return if (serviceUrl != null) {
            HttpSqlGenerationAgent(serviceUrl)
        } else {
            MockSqlGenerationAgent()
        }
    }
}
