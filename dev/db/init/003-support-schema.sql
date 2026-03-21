CREATE SCHEMA support;

CREATE TABLE support.tickets (
    ticket_id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES public.customers(customer_id),
    order_id BIGINT REFERENCES public.orders(order_id),
    subject TEXT NOT NULL,
    status TEXT NOT NULL CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE support.ticket_comments (
    comment_id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL REFERENCES support.tickets(ticket_id) ON DELETE CASCADE,
    author_type TEXT NOT NULL CHECK (author_type IN ('CUSTOMER', 'AGENT')),
    body TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_support_tickets_customer_id ON support.tickets(customer_id);
CREATE INDEX idx_support_tickets_order_id ON support.tickets(order_id);
CREATE INDEX idx_support_tickets_status ON support.tickets(status);
CREATE INDEX idx_support_ticket_comments_ticket_id ON support.ticket_comments(ticket_id);
