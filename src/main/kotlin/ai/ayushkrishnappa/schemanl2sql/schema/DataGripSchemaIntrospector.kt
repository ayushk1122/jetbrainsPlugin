package ai.ayushkrishnappa.schemanl2sql.schema

import com.intellij.openapi.project.Project

class DataGripSchemaIntrospector : SchemaIntrospector {
    override fun inspect(project: Project): SchemaInspectionResult {
        return SchemaInspectionResult(
            errors = listOf(
                "Live DataGrip schema introspection is not implemented yet.",
                "Next step: resolve the active data source and extract tables, columns, keys, and types from DataGrip database APIs.",
            ),
        )
    }
}
