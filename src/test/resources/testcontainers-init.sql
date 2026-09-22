DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;

CREATE TABLE categories (
    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE products (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(150)  NOT NULL,
    description TEXT,
    price       NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    stock       INTEGER        NOT NULL DEFAULT 0 CHECK (stock >= 0),
    category_id BIGINT REFERENCES categories (id),
    created_at  TIMESTAMP      NOT NULL DEFAULT now()
);

CREATE INDEX idx_products_category ON products (category_id);

CREATE TABLE users (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'USER' CHECK (role IN ('USER', 'ADMIN')),
    created_at    TIMESTAMP    NOT NULL DEFAULT now()
);

INSERT INTO categories (name) VALUES
    ('Îmbrăcăminte'),
    ('Accesorii'),
    ('Băuturi');

INSERT INTO products (name, description, price, stock, category_id) VALUES
    ('Tricou Impact',        'Tricou din bumbac cu logo Impact',        199.00,  40, (SELECT id FROM categories WHERE name = 'Îmbrăcăminte')),
    ('Cană de cafea',        'Cană ceramică 350 ml',                    129.00,  75, (SELECT id FROM categories WHERE name = 'Accesorii')),
    ('Sticlă de apă 750ml',  'Sticlă reutilizabilă din inox',           159.00,  60, (SELECT id FROM categories WHERE name = 'Băuturi'));
