ALTER TABLE users ADD COLUMN role VARCHAR(30);

-- Populate role based on user_type
UPDATE users SET role = user_type;

-- Fix the admin user if it exists (it might have been created as ATENDENTE)
-- UPDATE users SET role = 'ADMIN' WHERE email = 'admin@mecanica.com';

-- Set NOT NULL constraint
ALTER TABLE users ALTER COLUMN role SET NOT NULL;
