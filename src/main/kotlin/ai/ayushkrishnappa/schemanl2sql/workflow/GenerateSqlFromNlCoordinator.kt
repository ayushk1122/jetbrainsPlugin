package ai.ayushkrishnappa.schemanl2sql.workflow

import ai.ayushkrishnappa.schemanl2sql.agent.SqlGenerationAgent
import ai.ayushkrishnappa.schemanl2sql.agent.SqlGenerationRequest
import ai.ayushkrishnappa.schemanl2sql.editor.SqlEditorInserter
import ai.ayushkrishnappa.schemanl2sql.schema.SchemaIntrospector
import ai.ayushkrishnappa.schemanl2sql.validation.SqlValidator
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project

class GenerateSqlFromNlCoordinator(
    private val schemaIntrospector: SchemaIntrospector,
    private val agent: SqlGenerationAgent,
    private val validator: SqlValidator,
    private val editorInserter: SqlEditorInserter,
) {
    fun generate(project: Project, editor: Editor, prompt: String): GenerateSqlFromNlResult {
        val warnings = mutableListOf<String>()
        val errors = mutableListOf<String>()

        val schemaResult = schemaIntrospector.inspect(project)
        warnings += schemaResult.warnings
        errors += schemaResult.errors

        val schema = schemaResult.schema
        if (schema == null) {
            return GenerateSqlFromNlResult(warnings = warnings, errors = errors)
        }

        val generationResult = agent.generate(
            SqlGenerationRequest(
                prompt = prompt,
                schema = schema,
            ),
        )
        warnings += generationResult.warnings
        errors += generationResult.errors

        val sql = generationResult.sql
        if (sql == null) {
            return GenerateSqlFromNlResult(warnings = warnings, errors = errors)
        }

        val validationResult = validator.validate(project, schema, sql)
        warnings += validationResult.warnings
        errors += validationResult.errors

        if (!validationResult.isValid || errors.isNotEmpty()) {
            return GenerateSqlFromNlResult(warnings = warnings, errors = errors)
        }

        editorInserter.insert(project, editor, sql)
        return GenerateSqlFromNlResult(
            insertedSql = sql,
            warnings = warnings,
            errors = errors,
        )
    }
}

data class GenerateSqlFromNlResult(
    val insertedSql: String? = null,
    val warnings: List<String> = emptyList(),
    val errors: List<String> = emptyList(),
)
