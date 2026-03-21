package ai.ayushkrishnappa.schemanl2sql.schema.model

import kotlinx.serialization.Serializable

@Serializable
data class DatabaseSchema(
    val dialect: String? = null,
    val defaultSchema: String? = null,
    val tables: List<TableSchema> = emptyList(),
)

@Serializable
data class TableSchema(
    val schema: String? = null,
    val name: String,
    val columns: List<ColumnSchema>,
    val primaryKey: List<String> = emptyList(),
    val foreignKeys: List<ForeignKeySchema> = emptyList(),
)

@Serializable
data class ColumnSchema(
    val name: String,
    val type: String,
    val nullable: Boolean,
)

@Serializable
data class ForeignKeySchema(
    val column: String,
    val referencedSchema: String? = null,
    val referencedTable: String,
    val referencedColumn: String,
)
