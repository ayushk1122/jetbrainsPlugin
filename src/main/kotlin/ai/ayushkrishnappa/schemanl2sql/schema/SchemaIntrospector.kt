package ai.ayushkrishnappa.schemanl2sql.schema

import ai.ayushkrishnappa.schemanl2sql.schema.model.DatabaseSchema
import com.intellij.openapi.project.Project

interface SchemaIntrospector {
    fun inspect(project: Project): SchemaInspectionResult
}

data class SchemaInspectionResult(
    val schema: DatabaseSchema? = null,
    val warnings: List<String> = emptyList(),
    val errors: List<String> = emptyList(),
)
