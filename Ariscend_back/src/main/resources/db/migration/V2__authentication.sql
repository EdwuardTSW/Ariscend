-- This one-time reset is intentional: authentication changes ownership semantics.
-- CASCADE clears every user-owned relation; RESTART IDENTITY resets all truncated identities.
TRUNCATE TABLE users RESTART IDENTITY CASCADE;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'notes'
          AND column_name = 'content'
          AND data_type = 'bytea'
    ) THEN
        ALTER TABLE notes
            ALTER COLUMN content TYPE TEXT
            USING convert_from(content, 'UTF8');
    END IF;
END $$;

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS password_hash VARCHAR(60);

CREATE TABLE IF NOT EXISTS spring_session (
    primary_id CHAR(36) NOT NULL,
    session_id CHAR(36) NOT NULL,
    creation_time BIGINT NOT NULL,
    last_access_time BIGINT NOT NULL,
    max_inactive_interval INTEGER NOT NULL,
    expiry_time BIGINT NOT NULL,
    principal_name VARCHAR(100),
    CONSTRAINT spring_session_pk PRIMARY KEY (primary_id)
);

CREATE UNIQUE INDEX IF NOT EXISTS spring_session_ix1 ON spring_session (session_id);
CREATE INDEX IF NOT EXISTS spring_session_ix2 ON spring_session (expiry_time);
CREATE INDEX IF NOT EXISTS spring_session_ix3 ON spring_session (principal_name);

CREATE TABLE IF NOT EXISTS spring_session_attributes (
    session_primary_id CHAR(36) NOT NULL,
    attribute_name VARCHAR(200) NOT NULL,
    attribute_bytes BYTEA NOT NULL,
    CONSTRAINT spring_session_attributes_pk PRIMARY KEY (session_primary_id, attribute_name),
    CONSTRAINT spring_session_attributes_fk FOREIGN KEY (session_primary_id)
        REFERENCES spring_session (primary_id) ON DELETE CASCADE
);

-- TRUNCATE users CASCADE also clears global rows from this nullable referencing table.
INSERT INTO transaction_categories (name, type, system_defined, system_key, active, created_at)
VALUES
    ('Salario', 'INCOME', TRUE, 'INCOME_SALARY', TRUE, CURRENT_TIMESTAMP),
    ('Trabajo independiente', 'INCOME', TRUE, 'INCOME_FREELANCE', TRUE, CURRENT_TIMESTAMP),
    ('Ventas', 'INCOME', TRUE, 'INCOME_SALES', TRUE, CURRENT_TIMESTAMP),
    ('Inversiones', 'INCOME', TRUE, 'INCOME_INVESTMENTS', TRUE, CURRENT_TIMESTAMP),
    ('Regalos', 'INCOME', TRUE, 'INCOME_GIFTS', TRUE, CURRENT_TIMESTAMP),
    ('Otros ingresos', 'INCOME', TRUE, 'INCOME_OTHER', TRUE, CURRENT_TIMESTAMP),
    ('Alimentación', 'EXPENSE', TRUE, 'EXPENSE_FOOD', TRUE, CURRENT_TIMESTAMP),
    ('Transporte', 'EXPENSE', TRUE, 'EXPENSE_TRANSPORT', TRUE, CURRENT_TIMESTAMP),
    ('Vivienda', 'EXPENSE', TRUE, 'EXPENSE_HOUSING', TRUE, CURRENT_TIMESTAMP),
    ('Servicios', 'EXPENSE', TRUE, 'EXPENSE_SERVICES', TRUE, CURRENT_TIMESTAMP),
    ('Salud', 'EXPENSE', TRUE, 'EXPENSE_HEALTH', TRUE, CURRENT_TIMESTAMP),
    ('Educación', 'EXPENSE', TRUE, 'EXPENSE_EDUCATION', TRUE, CURRENT_TIMESTAMP),
    ('Entretenimiento', 'EXPENSE', TRUE, 'EXPENSE_ENTERTAINMENT', TRUE, CURRENT_TIMESTAMP),
    ('Compras', 'EXPENSE', TRUE, 'EXPENSE_SHOPPING', TRUE, CURRENT_TIMESTAMP),
    ('Metas y ahorro', 'EXPENSE', TRUE, 'EXPENSE_GOALS', TRUE, CURRENT_TIMESTAMP),
    ('Otros gastos', 'EXPENSE', TRUE, 'EXPENSE_OTHER', TRUE, CURRENT_TIMESTAMP)
ON CONFLICT (system_key) DO UPDATE SET
    name = EXCLUDED.name,
    type = EXCLUDED.type,
    system_defined = TRUE,
    user_id = NULL,
    active = TRUE;
