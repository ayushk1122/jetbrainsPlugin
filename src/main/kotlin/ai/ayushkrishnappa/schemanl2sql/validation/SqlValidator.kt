package ai.ayushkrishnappa.schemanl2sql.validation

import ai.ayushkrishnappa.schemanl2sql.schema.model.DatabaseSchema
import ai.ayushkrishnappa.schemanl2sql.schema.model.TableSchema
import com.intellij.openapi.project.Project

class SqlValidator {
    fun validate(project: Project, schema: DatabaseSchema, sql: String): SqlValidationResult {
        val warnings = mutableListOf<String>()
        val errors = mutableListOf<String>()

        if (schema.tables.isEmpty()) {
            warnings += "Schema validation is not active because no schema model was provided."
        }

        if (!sql.trim().endsWith(";")) {
            warnings += "Generated SQL does not end with a semicolon."
        }

        val tableReferences = extractTableReferences(sql)
        if (tableReferences.isEmpty()) {
            warnings += "No FROM or JOIN table references were found in the generated SQL."
        }

        val resolvedTables = mutableMapOf<String, TableSchema>()
        tableReferences.forEach { reference ->
            val table = schema.resolveTable(reference.tableName)
            if (table == null) {
                errors += "Unknown table reference '${reference.tableName}' in generated SQL."
            } else {
                resolvedTables[reference.alias ?: table.name] = table
                resolvedTables[table.name] = table
                table.schema?.let { schemaName ->
                    resolvedTables["$schemaName.${table.name}"] = table
                }
            }
        }

        extractAliasedColumnReferences(sql).forEach { reference ->
            if (schema.isQualifiedTableReference(reference.owner, reference.column)) {
                return@forEach
            }

            val table = resolvedTables[reference.owner]
                ?: schema.resolveTable(reference.owner)

            if (table == null) {
                errors += "Unknown table or alias '${reference.owner}' in column reference '${reference.owner}.${reference.column}'."
            } else if (table.columns.none { it.name.equals(reference.column, ignoreCase = true) }) {
                errors += "Unknown column '${reference.column}' on table '${table.qualifiedName()}'."
            }
        }

        return SqlValidationResult(
            isValid = sql.isNotBlank() && errors.isEmpty(),
            warnings = warnings,
            errors = errors.distinct(),
        )
    }

    private fun extractTableReferences(sql: String): List<TableReference> {
        val regex = Regex("""\b(from|join)\s+([a-zA-Z_][\w\."]*)(?:\s+(?:as\s+)?([a-zA-Z_][\w]*))?""", RegexOption.IGNORE_CASE)

        return regex.findAll(sql)
            .map { match ->
                TableReference(
                    tableName = normalizeIdentifier(match.groupValues[2]),
                    alias = match.groupValues.getOrNull(3)?.takeIf(String::isNotBlank),
                )
            }
            .toList()
    }

    private fun extractAliasedColumnReferences(sql: String): List<ColumnReference> {
        val regex = Regex("""\b([a-zA-Z_][\w]*)\.([a-zA-Z_][\w]*)\b""")

        return regex.findAll(sql)
            .map { match ->
                ColumnReference(
                    owner = normalizeIdentifier(match.groupValues[1]),
                    column = normalizeIdentifier(match.groupValues[2]),
                )
            }
            .toList()
    }

    private fun normalizeIdentifier(identifier: String): String {
        return identifier.trim().removePrefix("\"").removeSuffix("\"")
    }

    private fun DatabaseSchema.resolveTable(reference: String): TableSchema? {
        val normalized = normalizeIdentifier(reference)

        return tables.firstOrNull { table ->
            table.qualifiedName().equals(normalized, ignoreCase = true) ||
                table.name.equals(normalized, ignoreCase = true)
        }
    }

    private fun DatabaseSchema.isQualifiedTableReference(owner: String, name: String): Boolean {
        return tables.any { table ->
            table.schema.equals(owner, ignoreCase = true) &&
                table.name.equals(name, ignoreCase = true)
        }
    }

    private fun TableSchema.qualifiedName(): String {
        return listOfNotNull(schema, name).joinToString(".")
    }
}

data class SqlValidationResult(
    val isValid: Boolean,
    val warnings: List<String> = emptyList(),
    val errors: List<String> = emptyList(),
)

private data class TableReference(
    val tableName: String,
    val alias: String? = null,
)

private data class ColumnReference(
    val owner: String,
    val column: String,
)
