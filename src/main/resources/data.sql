INSERT INTO users (
    id,
    email,
    enabled,
    locked,
    password,
    role,
    username
) SELECT
    '6dc21cfd-f0a0-444e-a0ba-8a4212e67a4b',
    'admin@change-me.com',
    true,
    false,
    '$2a$10$eJKjKgSfs9Q4/d6hJnjw2ucaegROxdhczPiL4ofN1z3Jkk4U8xH8S',
    'SUPER_ADMIN',
    'admin'
WHERE NOT EXISTS (SELECT id FROM users WHERE id = '6dc21cfd-f0a0-444e-a0ba-8a4212e67a4b');