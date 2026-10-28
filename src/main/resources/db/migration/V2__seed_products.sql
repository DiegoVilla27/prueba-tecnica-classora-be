-- =============================================================================
-- V2 · Product catalog seed
-- The POS does not manage the catalog: it is loaded here and is the same in
-- every environment. Fixed UUIDs so the front-end MSW fixtures can match it.
-- =============================================================================

INSERT INTO products (id, code, name, price, created_at, updated_at) VALUES
    ('0b6e4c4e-1a2b-4c3d-8e9f-000000000001', '8410001000011', 'Leche entera 1L',              0.95, now(), now()),
    ('0b6e4c4e-1a2b-4c3d-8e9f-000000000002', '8410001000028', 'Pan de molde integral 460g',   1.65, now(), now()),
    ('0b6e4c4e-1a2b-4c3d-8e9f-000000000003', '8410001000035', 'Huevos frescos M (12 uds)',    2.79, now(), now()),
    ('0b6e4c4e-1a2b-4c3d-8e9f-000000000004', '8410001000042', 'Aceite de oliva virgen extra 1L', 8.95, now(), now()),
    ('0b6e4c4e-1a2b-4c3d-8e9f-000000000005', '8410001000059', 'Arroz redondo 1kg',            1.29, now(), now()),
    ('0b6e4c4e-1a2b-4c3d-8e9f-000000000006', '8410001000066', 'Yogur natural (pack 4)',       1.15, now(), now()),
    ('0b6e4c4e-1a2b-4c3d-8e9f-000000000007', '8410001000073', 'Café molido natural 250g',     3.49, now(), now()),
    ('0b6e4c4e-1a2b-4c3d-8e9f-000000000008', '8410001000080', 'Agua mineral 1,5L',            0.45, now(), now()),
    ('0b6e4c4e-1a2b-4c3d-8e9f-000000000009', '8410001000097', 'Tomate frito 400g',            0.89, now(), now()),
    ('0b6e4c4e-1a2b-4c3d-8e9f-000000000010', '8410001000103', 'Detergente líquido 30 lavados', 6.75, now(), now()),
    ('0b6e4c4e-1a2b-4c3d-8e9f-000000000011', '8410001000110', 'Bolsa de papel',               0.10, now(), now()),
    ('0b6e4c4e-1a2b-4c3d-8e9f-000000000012', '1001',          'Plátano de Canarias (unidad)', 0.35, now(), now());
