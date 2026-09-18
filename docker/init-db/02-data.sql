-- Lecția 7 — date de start pentru catalogul din container, identice cu
-- Lecția 2/insert_data.sql (fără secțiunea "Verificare rapidă", inutilă aici).

INSERT INTO categories (name) VALUES
    ('Îmbrăcăminte'),
    ('Accesorii'),
    ('Băuturi');

INSERT INTO products (name, description, price, stock, category_id) VALUES
    ('Tricou Impact',        'Tricou din bumbac cu logo Impact',        199.00,  40, (SELECT id FROM categories WHERE name = 'Îmbrăcăminte')),
    ('Hanorac cu glugă',     'Hanorac gros, unisex',                    499.00,  18, (SELECT id FROM categories WHERE name = 'Îmbrăcăminte')),
    ('Șapcă snapback',       'Șapcă reglabilă',                         179.00,  33, (SELECT id FROM categories WHERE name = 'Îmbrăcăminte')),
    ('Cană de cafea',        'Cană ceramică 350 ml',                    129.00,  75, (SELECT id FROM categories WHERE name = 'Accesorii')),
    ('Rucsac pentru laptop', 'Rucsac cu compartiment pentru laptop 15"', 899.00,  12, (SELECT id FROM categories WHERE name = 'Accesorii')),
    ('Set autocolante',      'Set de 10 autocolante',                    49.00, 200, (SELECT id FROM categories WHERE name = 'Accesorii')),
    ('Sticlă de apă 750ml',  'Sticlă reutilizabilă din inox',           159.00,  60, (SELECT id FROM categories WHERE name = 'Băuturi'));

-- Parolele sunt hash BCrypt. Valorile în clar: admin@impact.md -> admin123, user@impact.md -> user123.
INSERT INTO users (email, password_hash, role) VALUES
    ('admin@impact.md', '$2b$10$77pyjtkVnuT36G3xsMbBVObven4oVMpAs2DOyFGaA0UWL2LwFe5Hu', 'ADMIN'),
    ('user@impact.md',  '$2b$10$iMWVkUMGPEWtwjBhLQmMxer1aTcqh7bZaWbFlaMANlvV3e9QCT5vm', 'USER');
