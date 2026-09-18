INSERT INTO categories (name) VALUES ('Test Cat A'), ('Test Cat B');

INSERT INTO products (name, description, price, stock, category_id, created_at) VALUES
 ('Test Product 1', 'primul', 10.00, 5, 1, CURRENT_TIMESTAMP),
 ('Test Product 2', 'al doilea', 20.50, 0, 2, CURRENT_TIMESTAMP);
