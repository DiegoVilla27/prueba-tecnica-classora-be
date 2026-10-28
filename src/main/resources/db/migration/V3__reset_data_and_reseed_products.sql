-- =============================================================================
-- V3 · Reset data and reseed the product catalog
-- Wipes every sale, user and product and loads a fixed catalog of 20 products
-- with valid EAN-13 codes (GS1 Spain prefix 84, check digit computed). The same
-- in every environment; fixed UUIDs so the front-end MSW fixtures can match it.
-- The single seed cashier is recreated on startup by DatabaseSeeder.
-- =============================================================================

DELETE FROM sale_lines;
DELETE FROM sales;
DELETE FROM users;
DELETE FROM products;

INSERT INTO products (id, code, name, price, created_at, updated_at) VALUES
    ('0b6e4c4e-1a2b-4c3d-8e9f-100000000001', '8412345000010', 'Leche entera 1L',                     0.95, now(), now()),
    ('0b6e4c4e-1a2b-4c3d-8e9f-100000000002', '8412345000027', 'Pan de molde integral 460g',          1.65, now(), now()),
    ('0b6e4c4e-1a2b-4c3d-8e9f-100000000003', '8412345000034', 'Huevos frescos M (12 uds)',           2.79, now(), now()),
    ('0b6e4c4e-1a2b-4c3d-8e9f-100000000004', '8412345000041', 'Aceite de oliva virgen extra 1L',     8.95, now(), now()),
    ('0b6e4c4e-1a2b-4c3d-8e9f-100000000005', '8412345000058', 'Arroz redondo 1kg',                   1.29, now(), now()),
    ('0b6e4c4e-1a2b-4c3d-8e9f-100000000006', '8412345000065', 'Yogur natural (pack 4)',              1.15, now(), now()),
    ('0b6e4c4e-1a2b-4c3d-8e9f-100000000007', '8412345000072', 'Café molido natural 250g',            3.49, now(), now()),
    ('0b6e4c4e-1a2b-4c3d-8e9f-100000000008', '8412345000089', 'Agua mineral 1,5L',                   0.45, now(), now()),
    ('0b6e4c4e-1a2b-4c3d-8e9f-100000000009', '8412345000096', 'Tomate frito 400g',                   0.89, now(), now()),
    ('0b6e4c4e-1a2b-4c3d-8e9f-100000000010', '8412345000102', 'Detergente líquido 30 lavados',       6.75, now(), now()),
    ('0b6e4c4e-1a2b-4c3d-8e9f-100000000011', '8412345000119', 'Bolsa de papel',                      0.10, now(), now()),
    ('0b6e4c4e-1a2b-4c3d-8e9f-100000000012', '8412345000126', 'Plátanos de Canarias 1kg',            2.19, now(), now()),
    ('0b6e4c4e-1a2b-4c3d-8e9f-100000000013', '8412345000133', 'Macarrones 500g',                     0.85, now(), now()),
    ('0b6e4c4e-1a2b-4c3d-8e9f-100000000014', '8412345000140', 'Atún claro en aceite (pack 3)',       2.95, now(), now()),
    ('0b6e4c4e-1a2b-4c3d-8e9f-100000000015', '8412345000157', 'Queso semicurado 250g',               3.25, now(), now()),
    ('0b6e4c4e-1a2b-4c3d-8e9f-100000000016', '8412345000164', 'Jamón cocido extra 200g',             2.40, now(), now()),
    ('0b6e4c4e-1a2b-4c3d-8e9f-100000000017', '8412345000171', 'Galletas María 800g',                 1.99, now(), now()),
    ('0b6e4c4e-1a2b-4c3d-8e9f-100000000018', '8412345000188', 'Zumo de naranja 1L',                  1.49, now(), now()),
    ('0b6e4c4e-1a2b-4c3d-8e9f-100000000019', '8412345000195', 'Papel higiénico doble capa (12 rollos)', 4.50, now(), now()),
    ('0b6e4c4e-1a2b-4c3d-8e9f-100000000020', '8412345000201', 'Cerveza lata 33cl (pack 6)',          3.89, now(), now());
