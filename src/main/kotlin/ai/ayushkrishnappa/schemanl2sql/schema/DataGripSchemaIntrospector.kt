package ai.ayushkrishnappa.schemanl2sql.schema

import ai.ayushkrishnappa.schemanl2sql.schema.model.ColumnSchema
import ai.ayushkrishnappa.schemanl2sql.schema.model.DatabaseSchema
import ai.ayushkrishnappa.schemanl2sql.schema.model.ForeignKeySchema
import ai.ayushkrishnappa.schemanl2sql.schema.model.TableSchema
import com.intellij.database.dataSource.LocalDataSource
import com.intellij.database.dataSource.LocalDataSourceManager
import com.intellij.database.model.DasTable
import com.intellij.database.model.DasTypedObject
import com.intellij.database.util.DasUtil
import com.intellij.openapi.project.Project

class DataGripSchemaIntrospector : SchemaIntrospector {
    private val excludedSchemas = setOf("information_schema", "pg_catalog")

    override fun inspect(project: Project): SchemaInspectionResult {
        val manager = LocalDataSourceManager.getInstance(project)
        val dataSources = manager.dataSources.filterNot(LocalDataSource::isTemporary)

        if (dataSources.isEmpty()) {
            return SchemaInspectionResult(
                errors = listOf("No DataGrip data sources are configured for this project."),
            )
        }

        val warnings = mutableListOf<String>()
        val chosenDataSource = chooseDataSource(manager, dataSources, warnings)

        if (manager.isLoading(chosenDataSource)) {
            warnings += "Data source '${chosenDataSource.name}' is still loading. Results may be incomplete."
        }

        val allTables = DasUtil.getTables(chosenDataSource)
            .filterIsInstance<DasTable>()
            .filterNot(DasTable::isSystem)
            .filterNot(DasTable::isTemporary)
            .toList()

        val tables = filterUserTables(allTables, warnings)

        if (tables.isEmpty()) {
            return SchemaInspectionResult(
                dataSourceName = chosenDataSource.name,
                warnings = warnings,
                errors = listOf(
                    "No user tables were found for data source '${chosenDataSource.name}'.",
                    "Make sure the data source is synchronized and the target schema is selected for introspection.",
                ),
            )
        }

        return SchemaInspectionResult(
            dataSourceName = chosenDataSource.name,
            schema = DatabaseSchema(
                dialect = chosenDataSource.dbms.toString(),
                tables = tables
                    .map(::toTableSchema)
                    .sortedBy(TableSchema::name),
            ),
            warnings = warnings,
        )
    }

    private fun chooseDataSource(
        manager: LocalDataSourceManager,
        dataSources: List<LocalDataSource>,
        warnings: MutableList<String>,
    ): LocalDataSource {
        val readyDataSources = dataSources.filterNot(manager::isLoading)
        val chosen = readyDataSources.firstOrNull() ?: dataSources.first()

        if (dataSources.size > 1) {
            warnings += buildString {
                append("Multiple data sources are configured. ")
                append("Using '${chosen.name}' for schema introspection. ")
                append("Explicit data source selection is not implemented yet.")
            }
        }

        return chosen
    }

    private fun filterUserTables(
        tables: List<DasTable>,
        warnings: MutableList<String>,
    ): List<DasTable> {
        val nonSystemSchemaTables = tables.filterNot { table ->
            schemaNameOf(table) in excludedSchemas
        }

        val publicTables = nonSystemSchemaTables.filter { table ->
            schemaNameOf(table) == "public"
        }

        if (publicTables.isNotEmpty()) {
            warnings += "Filtering schema snapshot to user tables in the 'public' schema."
            return publicTables
        }

        if (nonSystemSchemaTables.size != tables.size) {
            warnings += "Excluded PostgreSQL system schemas from the schema snapshot."
        }

        return nonSystemSchemaTables
    }

    private fun schemaNameOf(table: DasTable): String? = DasUtil.getSchema(table)

    private fun toTableSchema(table: DasTable): TableSchema {
        val columns = DasUtil.getColumns(table)
            .filterIsInstance<DasTypedObject>()
            .map { column ->
                ColumnSchema(
                    name = column.name,
                    type = column.dataType?.specification ?: "<unknown>",
                    nullable = !column.isNotNull,
                )
            }
            .sortedBy(ColumnSchema::name)
            .toList()

        val primaryKey = DasUtil.getPrimaryKey(table)
            ?.columnsRef
            ?.names()
            ?.toList()
            .orEmpty()

        val foreignKeys = DasUtil.getForeignKeys(table)
            .flatMap { foreignKey ->
                val localColumns = foreignKey.columnsRef.names().toList()
                val referencedColumns = foreignKey.refColumns.resolveObjects().map(DasTypedObject::getName).toList()

                localColumns.mapIndexed { index, localColumn ->
                    ForeignKeySchema(
                        column = localColumn,
                        referencedTable = foreignKey.refTableName,
                        referencedColumn = referencedColumns.getOrElse(index) { "<unknown>" },
                    )
                }
            }
            .sortedWith(compareBy(ForeignKeySchema::column, ForeignKeySchema::referencedTable, ForeignKeySchema::referencedColumn))
            .toList()

        return TableSchema(
            name = table.name,
            columns = columns,
            primaryKey = primaryKey,
            foreignKeys = foreignKeys,
        )
    }
}
