package ai.ayushkrishnappa.schemanl2sql.validation

import ai.ayushkrishnappa.schemanl2sql.schema.model.DatabaseSchema
import com.intellij.openapi.project.Project

class SqlValidator {
    fun validate(project: Project, schema: DatabaseSchema, sql: String): SqlValidationResult {
        val warnings = mutableListOf<String>()

        if (schema.tables.isEmpty()) {
            warnings += "Schema validation is not active because no schema model was provided."
        }

        if (!sql.trim().endsWith(";")) {
            warnings += "Generated SQL does not end with a semicolon."
        }

        return SqlValidationResult(
            isValid = sql.isNotBlank(),
            warnings = warnings,
        )
    }
}

data class SqlValidationResult(
    val isValid: Boolean,
    val warnings: List<String> = emptyList(),
    val errors: List<String> = emptyList(),
)
