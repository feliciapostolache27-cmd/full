-- Lecția 7 — rulat automat de imaginea oficială postgres la primul start al
-- containerului (docker-entrypoint-initdb.d), pe baza de date creată deja de
-- variabila de mediu POSTGRES_DB. Conținut identic cu Lecția 2/create_tables.sql —
-- copiat aici ca acest repo (referința de backend) să rămână de sine stătător,
-- fără să depindă de un folder din afara lui.

DROP TABLE IF EXISTS refresh_tokens;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;

CREATE TABLE categories (
    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE products (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name                VARCHAR(150)  NOT NULL,
    description         TEXT,
    price               NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    stock               INTEGER        NOT NULL DEFAULT 0 CHECK (stock >= 0),
    category_id         BIGINT REFERENCES categories (id),
    -- Lecția 11 — reducere manuală per produs, setată de admin; NULL = fără
    -- reducere manuală (produsul rămâne supus regulii de categorie, dacă există).
    discount_percentage NUMERIC(5, 2) CHECK (discount_percentage >= 0 AND discount_percentage <= 100),
    created_at          TIMESTAMP      NOT NULL DEFAULT now()
);

CREATE INDEX idx_products_category ON products (category_id);

CREATE TABLE users (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'USER' CHECK (role IN ('USER', 'ADMIN')),
    created_at    TIMESTAMP    NOT NULL DEFAULT now()
);

-- Lecția 11 — refresh tokens ținute în baza de date, nu într-un JWT: un JWT
-- stateless nu poate fi revocat înainte de expirare, un rând dintr-un tabel, da.
CREATE TABLE refresh_tokens (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    token      VARCHAR(255) NOT NULL UNIQUE,
    user_id    BIGINT       NOT NULL REFERENCES users (id),
    expires_at TIMESTAMP    NOT NULL,
    revoked    BOOLEAN      NOT NULL DEFAULT false,
    created_at TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens (user_id);
