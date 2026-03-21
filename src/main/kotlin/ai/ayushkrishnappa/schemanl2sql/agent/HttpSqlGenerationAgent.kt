package ai.ayushkrishnappa.schemanl2sql.agent

import ai.ayushkrishnappa.schemanl2sql.agent.protocol.SqlGenerationHttpRequest
import ai.ayushkrishnappa.schemanl2sql.agent.protocol.SqlGenerationHttpResponse
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration

class HttpSqlGenerationAgent(
    private val serviceUrl: String,
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .version(HttpClient.Version.HTTP_1_1)
        .build(),
    private val json: Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    },
) : SqlGenerationAgent {
    override fun generate(request: SqlGenerationRequest): SqlGenerationResult {
        return runCatching {
            val payload = json.encodeToString(
                SqlGenerationHttpRequest.serializer(),
                SqlGenerationHttpRequest(
                    prompt = request.prompt,
                    dataSourceName = request.dataSourceName,
                    schema = request.schema,
                ),
            )
            val httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(normalizeUrl(serviceUrl)))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofByteArray(payload.toByteArray(StandardCharsets.UTF_8)))
                .build()

            val response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() !in 200..299) {
                return SqlGenerationResult(
                    model = "http",
                    errors = listOf(
                        "External SQL generation service returned HTTP ${response.statusCode()}.",
                        "Request body length: ${payload.length} characters.",
                        response.body().ifBlank { "No response body returned." },
                    ),
                )
            }

            val decoded = json.decodeFromString<SqlGenerationHttpResponse>(response.body())
            SqlGenerationResult(
                sql = decoded.sql,
                model = decoded.model,
                warnings = decoded.warnings,
                errors = decoded.errors,
            )
        }.getOrElse { error ->
            SqlGenerationResult(
                model = "http",
                errors = listOf(
                    "Failed to call external SQL generation service.",
                    error.message ?: error::class.simpleName.orEmpty(),
                ).filter(String::isNotBlank),
            )
        }
    }

    private fun normalizeUrl(rawUrl: String): String {
        return rawUrl.trimEnd('/') + "/generate"
    }
}
