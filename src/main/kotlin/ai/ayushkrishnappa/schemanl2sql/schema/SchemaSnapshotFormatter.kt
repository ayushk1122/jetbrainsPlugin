package ai.ayushkrishnappa.schemanl2sql.schema

import ai.ayushkrishnappa.schemanl2sql.schema.model.DatabaseSchema

object SchemaSnapshotFormatter {
    fun format(result: SchemaInspectionResult): String {
        val lines = mutableListOf<String>()

        result.dataSourceName?.let { lines += "Data source: $it" }
        result.schema?.dialect?.let { lines += "Dialect: $it" }
        result.schema?.defaultSchema?.let { lines += "Default schema: $it" }

        if (result.warnings.isNotEmpty()) {
            lines += ""
            lines += "Warnings:"
            lines += result.warnings.map { "- $it" }
        }

        if (result.errors.isNotEmpty()) {
            lines += ""
            lines += "Errors:"
            lines += result.errors.map { "- $it" }
        }

        result.schema?.let { schema ->
            lines += ""
            lines += "Tables (${schema.tables.size}):"
            lines += renderTables(schema)
        }

        return lines.joinToString("\n").ifBlank { "No schema information available." }
    }

    private fun renderTables(schema: DatabaseSchema): List<String> {
        return schema.tables.flatMap { table ->
            buildList {
                add("* ${qualifiedTableName(table.schema, table.name)}")

                if (table.primaryKey.isNotEmpty()) {
                    add("  PK: ${table.primaryKey.joinToString(", ")}")
                }

                addAll(table.columns.map { column ->
                    val nullability = if (column.nullable) "NULL" else "NOT NULL"
                    "  - ${column.name}: ${column.type} $nullability"
                })

                if (table.foreignKeys.isNotEmpty()) {
                    addAll(table.foreignKeys.map { foreignKey ->
                        "  FK: ${foreignKey.column} -> ${qualifiedTableName(foreignKey.referencedSchema, foreignKey.referencedTable)}.${foreignKey.referencedColumn}"
                    })
                }
            }
        }
    }

    private fun qualifiedTableName(schema: String?, table: String): String {
        return listOfNotNull(schema, table).joinToString(".")
    }
}
