INSERT INTO customers (first_name, last_name, email, state_code, created_at) VALUES
    ('Alice', 'Johnson', 'alice.johnson@example.com', 'CA', '2025-11-01T10:00:00Z'),
    ('Bob', 'Smith', 'bob.smith@example.com', 'NY', '2025-11-03T12:15:00Z'),
    ('Carla', 'Nguyen', 'carla.nguyen@example.com', 'TX', '2025-11-05T09:30:00Z'),
    ('Daniel', 'Kim', 'daniel.kim@example.com', 'WA', '2025-11-08T14:45:00Z'),
    ('Eva', 'Martinez', 'eva.martinez@example.com', 'CA', '2025-11-11T16:20:00Z');

INSERT INTO categories (name, created_at) VALUES
    ('Electronics', '2025-11-01T00:00:00Z'),
    ('Home Office', '2025-11-01T00:00:00Z'),
    ('Accessories', '2025-11-01T00:00:00Z');

INSERT INTO products (category_id, sku, name, unit_price, is_active, created_at) VALUES
    (1, 'ELEC-1001', 'Wireless Mouse', 29.99, TRUE, '2025-11-02T00:00:00Z'),
    (1, 'ELEC-1002', 'Mechanical Keyboard', 119.00, TRUE, '2025-11-02T00:00:00Z'),
    (1, 'ELEC-1003', '27 Inch Monitor', 289.50, TRUE, '2025-11-02T00:00:00Z'),
    (2, 'HOME-2001', 'Standing Desk', 499.00, TRUE, '2025-11-02T00:00:00Z'),
    (2, 'HOME-2002', 'Ergonomic Chair', 349.00, TRUE, '2025-11-02T00:00:00Z'),
    (3, 'ACCS-3001', 'USB-C Dock', 89.99, TRUE, '2025-11-02T00:00:00Z'),
    (3, 'ACCS-3002', 'Laptop Sleeve', 39.50, TRUE, '2025-11-02T00:00:00Z');

INSERT INTO orders (customer_id, status, placed_at, notes, created_at) VALUES
    (1, 'PAID', '2026-01-05T13:10:00Z', 'Priority customer', '2026-01-05T13:10:00Z'),
    (2, 'FULFILLED', '2026-01-10T17:45:00Z', NULL, '2026-01-10T17:45:00Z'),
    (1, 'PENDING', '2026-01-18T08:25:00Z', NULL, '2026-01-18T08:25:00Z'),
    (3, 'CANCELLED', '2026-01-22T11:05:00Z', 'Customer changed mind', '2026-01-22T11:05:00Z'),
    (4, 'PAID', '2026-02-02T15:30:00Z', NULL, '2026-02-02T15:30:00Z'),
    (5, 'FULFILLED', '2026-02-09T19:40:00Z', NULL, '2026-02-09T19:40:00Z'),
    (2, 'PAID', '2026-02-14T09:15:00Z', 'Gift order', '2026-02-14T09:15:00Z');

INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
    (1, 2, 1, 119.00),
    (1, 6, 1, 89.99),
    (2, 4, 1, 499.00),
    (2, 1, 2, 29.99),
    (3, 5, 1, 349.00),
    (4, 7, 1, 39.50),
    (5, 3, 2, 289.50),
    (5, 1, 1, 29.99),
    (6, 4, 1, 499.00),
    (6, 6, 2, 89.99),
    (7, 2, 1, 119.00),
    (7, 3, 1, 289.50);
