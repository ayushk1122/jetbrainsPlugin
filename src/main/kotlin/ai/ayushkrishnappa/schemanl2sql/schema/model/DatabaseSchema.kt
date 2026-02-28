package ai.ayushkrishnappa.schemanl2sql.schema.model

data class DatabaseSchema(
    val dialect: String? = null,
    val tables: List<TableSchema> = emptyList(),
)

data class TableSchema(
    val name: String,
    val columns: List<ColumnSchema>,
    val primaryKey: List<String> = emptyList(),
    val foreignKeys: List<ForeignKeySchema> = emptyList(),
)

data class ColumnSchema(
    val name: String,
    val type: String,
    val nullable: Boolean,
)

data class ForeignKeySchema(
    val column: String,
    val referencedTable: String,
    val referencedColumn: String,
)
