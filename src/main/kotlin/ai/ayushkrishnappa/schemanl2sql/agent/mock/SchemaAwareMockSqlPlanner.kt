package ai.ayushkrishnappa.schemanl2sql.agent.mock

import ai.ayushkrishnappa.schemanl2sql.agent.SqlGenerationRequest
import ai.ayushkrishnappa.schemanl2sql.schema.model.DatabaseSchema
import ai.ayushkrishnappa.schemanl2sql.schema.model.TableSchema

class SchemaAwareMockSqlPlanner {
    fun plan(request: SqlGenerationRequest): MockSqlPlanResult {
        val schema = request.schema
        val prompt = request.prompt.lowercase()

        val sql = when {
            prompt.contains("revenue") && prompt.contains("customer") -> generateRevenueByCustomerQuery(schema)
            prompt.contains("recent") && prompt.contains("order") -> generateRecentOrdersQuery(schema)
            prompt.contains("product") && prompt.contains("category") -> generateProductsByCategoryQuery(schema)
            prompt.contains("customer") && (prompt.contains("california") || prompt.contains(" ca ")) -> generateCustomersByStateQuery(schema, "CA")
            else -> generateFallbackQuery(schema)
        }

        if (sql == null) {
            return MockSqlPlanResult(
                errors = listOf("The mock planner could not produce SQL for the current schema."),
            )
        }

        return MockSqlPlanResult(
            sql = sql,
            warnings = listOf("SQL was generated from deterministic schema-aware templates."),
        )
    }

    private fun generateRevenueByCustomerQuery(schema: DatabaseSchema): String? {
        val customers = schema.table("customers") ?: return null
        val orders = schema.table("orders") ?: return null
        val orderItems = schema.table("order_items") ?: return null

        return """
            SELECT
                c.customer_id,
                c.first_name,
                c.last_name,
                SUM(oi.quantity * oi.unit_price) AS total_revenue
            FROM ${customers.qualifiedName()} c
            JOIN ${orders.qualifiedName()} o
              ON o.customer_id = c.customer_id
            JOIN ${orderItems.qualifiedName()} oi
              ON oi.order_id = o.order_id
            GROUP BY c.customer_id, c.first_name, c.last_name
            ORDER BY total_revenue DESC;
        """.trimIndent()
    }

    private fun generateRecentOrdersQuery(schema: DatabaseSchema): String? {
        val orders = schema.table("orders") ?: return null

        return """
            SELECT
                o.order_id,
                o.customer_id,
                o.status,
                o.placed_at
            FROM ${orders.qualifiedName()} o
            ORDER BY o.placed_at DESC
            LIMIT 10;
        """.trimIndent()
    }

    private fun generateProductsByCategoryQuery(schema: DatabaseSchema): String? {
        val products = schema.table("products") ?: return null
        val categories = schema.table("categories") ?: return null

        return """
            SELECT
                p.product_id,
                p.name,
                p.unit_price,
                c.name AS category_name
            FROM ${products.qualifiedName()} p
            JOIN ${categories.qualifiedName()} c
              ON c.category_id = p.category_id
            ORDER BY c.name, p.name;
        """.trimIndent()
    }

    private fun generateCustomersByStateQuery(schema: DatabaseSchema, stateCode: String): String? {
        val customers = schema.table("customers") ?: return null

        return """
            SELECT
                c.customer_id,
                c.first_name,
                c.last_name,
                c.email,
                c.state_code
            FROM ${customers.qualifiedName()} c
            WHERE c.state_code = '$stateCode'
            ORDER BY c.last_name, c.first_name;
        """.trimIndent()
    }

    private fun generateFallbackQuery(schema: DatabaseSchema): String? {
        val table = schema.tables.firstOrNull() ?: return null

        return """
            SELECT *
            FROM ${table.qualifiedName()}
            LIMIT 25;
        """.trimIndent()
    }

    private fun DatabaseSchema.table(name: String): TableSchema? {
        return tables.firstOrNull { it.name.equals(name, ignoreCase = true) }
    }

    private fun TableSchema.qualifiedName(): String {
        return listOfNotNull(schema, name).joinToString(".")
    }
}

data class MockSqlPlanResult(
    val sql: String? = null,
    val warnings: List<String> = emptyList(),
    val errors: List<String> = emptyList(),
)
