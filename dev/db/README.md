# Dev Database

This folder contains the canonical PostgreSQL database for local plugin development.

The schema is intentionally small but realistic enough to exercise:

- table and column introspection
- primary and foreign key discovery
- common analytical joins
- timestamp and numeric filtering
- ambiguous natural language prompts that should require clarification later

## Quick Start

From the repository root:

```powershell
docker compose -f dev/db/docker-compose.yml up -d
```

Connection details:

- host: `localhost`
- port: `5433`
- database: `schema_nl2sql_dev`
- user: `schema_nl2sql`
- password: `schema_nl2sql`

The initialization scripts run only on first container creation. If you need a clean reset:

```powershell
docker compose -f dev/db/docker-compose.yml down -v
docker compose -f dev/db/docker-compose.yml up -d
```

## Schema Shape

The initial schema models a small commerce domain:

- `customers`
- `categories`
- `products`
- `orders`
- `order_items`

This gives the plugin a stable base for joins, aggregations, date filters, and relationship traversal.

## Next Expansion Paths

When you need more complexity later, add:

- `payments` for revenue vs. order total semantics
- `addresses` for one-to-many joins
- `inventory_movements` for event-style queries
- soft deletes and nullable foreign keys
- views and materialized views

## Acceptance Prompts

Use [`sample-prompts.md`](C:\Users\Ayush\IdeaProjects\schema-nl2sql\dev\db\sample-prompts.md) as the running set of natural-language test cases for the plugin.
