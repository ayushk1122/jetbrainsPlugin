INSERT INTO support.tickets (customer_id, order_id, subject, status, created_at) VALUES
    (1, 1, 'Need invoice copy', 'RESOLVED', '2026-02-11T10:00:00Z'),
    (2, 7, 'Keyboard arrived with damaged box', 'IN_PROGRESS', '2026-02-16T09:45:00Z'),
    (5, NULL, 'Question about standing desk assembly', 'OPEN', '2026-02-18T14:20:00Z');

INSERT INTO support.ticket_comments (ticket_id, author_type, body, created_at) VALUES
    (1, 'CUSTOMER', 'Can you send me a PDF invoice for my order?', '2026-02-11T10:05:00Z'),
    (1, 'AGENT', 'Invoice sent to your email on file.', '2026-02-11T10:18:00Z'),
    (2, 'CUSTOMER', 'The outer package was damaged, but the keyboard seems fine.', '2026-02-16T09:48:00Z'),
    (2, 'AGENT', 'Thanks for confirming. We can still send a replacement if needed.', '2026-02-16T11:00:00Z'),
    (3, 'CUSTOMER', 'Does the desk come with assembly tools?', '2026-02-18T14:22:00Z');
