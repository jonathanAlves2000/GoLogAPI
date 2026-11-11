-- =========================================================================
-- GoLog — RESET DO BANCO DE DEMONSTRAÇÃO (destrutivo!)
--
-- Apaga TODOS os dados e recria apenas o esqueleto que precisa de IDs fixos:
--   * Tenant Master GoLog (id usado pelo front e por scripts) + admin@admin.com
--   * Tenant Cultiva com o UUID que o backend da Cultiva já usa (GOLOG_TENANT_ID)
--   * Os 3 motoristas/veículos/conjuntos/escalas da Cultiva com os UUIDs do GoLogDriverSeeder deles
--
-- Todo o resto (catálogos, frota, clientes, usuários, escalas e remessas) é criado pela API em
-- seed_demo.py, passando pelas mesmas validações do sistema.
-- =========================================================================
BEGIN;

TRUNCATE TABLE
    shipment_visit_type_table, shipment_demand_table, occurrence_table, route_stop_table, telemetry_table,
    transport_table, shipment_table, collect_table, work_schedule, equipament_group_visit_type_table,
    group_transport_type_table, equipament_capacity_table, equipament_group_table, tractor_table,
    trailer_table, equipament_table, driver_table, optimization_profile_table, visit_type_rule_table,
    visit_type_table, demand_type_table, work_shift_template_table, shipment_type, type_transport_table,
    powerbi_token, auth_log, user_table, company_table, address_table
RESTART IDENTITY CASCADE;

-- ---------------------------------------------------------------- GoLog (Master)
INSERT INTO address_table (id, active, created_at, created_by, updated_at, updated_by, street, number, district,
                           city, state, country, cep, complement, latitude, longitude, company_id)
VALUES ('a1000000-0000-4000-8000-000000000001', true, now(), 'seed', now(), 'seed', 'Avenida Doutor Ângelo Franzini',
        '655', 'Jardim Belvedere', 'Araras', 'SP', 'Brasil', '13601-110', 'Centro de Distribuição',
        '-22.357202', '-47.383871', NULL);

INSERT INTO company_table (id, active, created_at, created_by, updated_at, updated_by, legal_name, cnpj_cpf, email,
                           phone_number, is_cliente, is_master, company_type, address_id, webhook_active)
VALUES ('7f564f96-d90f-42cc-beb2-e37cf63a324d', true, now(), 'seed', now(), 'seed', 'GoLog Transportes e Logística',
        '45123987000110', 'contato@golog.com.br', '(19)3541-2200', false, true, 'TENANT_MASTER',
        'a1000000-0000-4000-8000-000000000001', true);

UPDATE address_table SET company_id = '7f564f96-d90f-42cc-beb2-e37cf63a324d'
WHERE id = 'a1000000-0000-4000-8000-000000000001';

-- admin@admin.com (ADMIN do Master) — credencial de desenvolvimento já usada pelo projeto
INSERT INTO user_table (id, active, created_at, created_by, updated_at, updated_by, name, email, password, cpf,
                        user_profile, company_id)
VALUES ('a0000000-0000-4000-8000-000000000001', true, now(), 'seed', now(), 'seed', 'Administrador GoLog',
        'admin@admin.com', '$2a$10$a7jjJJyJxBDqJ74upx5mqeTWfvkhFSrUqmad4fJsyHb07H7YkiPk2', '39053344705', 'ADMIN',
        '7f564f96-d90f-42cc-beb2-e37cf63a324d');

-- ---------------------------------------------------------------- Cultiva (tenant de integração)
INSERT INTO address_table (id, active, created_at, created_by, updated_at, updated_by, street, number, district,
                           city, state, country, cep, complement, latitude, longitude, company_id)
VALUES ('c1000000-0000-4000-8000-000000000001', true, now(), 'seed', now(), 'seed', 'Rua XV de Novembro', '1000',
        'Centro', 'Blumenau', 'SC', 'Brasil', '89010-001', 'Hub Cultiva', '-26.915228', '-49.069215', NULL);

INSERT INTO company_table (id, active, created_at, created_by, updated_at, updated_by, legal_name, cnpj_cpf, email,
                           phone_number, is_cliente, is_master, company_type, address_id, webhook_active)
VALUES ('5647b3d4-f42f-4905-a617-896afa8dccef', true, now(), 'seed', now(), 'seed', 'Cultiva Marketplace Agro Ltda',
        '52384917000160', 'integracao@cultiva.com', '(47)3322-1100', false, false, 'TENANT_HEADQUARTER',
        'c1000000-0000-4000-8000-000000000001', true);

UPDATE address_table SET company_id = '5647b3d4-f42f-4905-a617-896afa8dccef'
WHERE id = 'c1000000-0000-4000-8000-000000000001';

-- Motoristas Cultiva (UUIDs e senha espelham backend/database/seeders/GoLogDriverSeeder.php da Cultiva)
INSERT INTO user_table (id, active, created_at, created_by, updated_at, updated_by, name, email, password, cpf,
                        user_profile, company_id) VALUES
 ('5c3d2b39-c067-4c0c-900d-2675f6c819d0', true, now(), 'seed', now(), 'seed', 'Henrique Costa Motorista',
  'henrique.costa@cultiva.com', '$2a$10$rvc4M2ku2nZJaP5/t68krudliW.tIOHEpHsLaG770DKI2zvTftZXW', '52948173060', 'DRIVER',
  '5647b3d4-f42f-4905-a617-896afa8dccef'),
 ('ae047dbf-71a1-42c9-8962-4d81458dd76d', true, now(), 'seed', now(), 'seed', 'Camila Rocha Motorista',
  'camila.rocha@cultiva.com', '$2a$10$rvc4M2ku2nZJaP5/t68krudliW.tIOHEpHsLaG770DKI2zvTftZXW', '34729185005', 'DRIVER',
  '5647b3d4-f42f-4905-a617-896afa8dccef'),
 ('0cd342e7-64bb-42a2-be08-82796ef61495', true, now(), 'seed', now(), 'seed', 'Thiago Almeida Motorista',
  'thiago.almeida@cultiva.com', '$2a$10$rvc4M2ku2nZJaP5/t68krudliW.tIOHEpHsLaG770DKI2zvTftZXW', '28471936003', 'DRIVER',
  '5647b3d4-f42f-4905-a617-896afa8dccef');

INSERT INTO driver_table (id, active, created_at, created_by, updated_at, updated_by, cnh_number, cnh_expiration,
                          cost_per_hour, user_id, company_id) VALUES
 ('7769e02b-87d9-475f-b400-6c4df77d14aa', true, now(), 'seed', now(), 'seed', '74829163504', '2029-06-30', 38.0,
  '5c3d2b39-c067-4c0c-900d-2675f6c819d0', '5647b3d4-f42f-4905-a617-896afa8dccef'),
 ('bc936c40-f0c2-45a7-bb69-721a10888cd4', true, now(), 'seed', now(), 'seed', '63928174500', '2028-11-30', 36.5,
  'ae047dbf-71a1-42c9-8962-4d81458dd76d', '5647b3d4-f42f-4905-a617-896afa8dccef'),
 ('63a2dc1a-4362-453c-a753-20c42e30bb4a', true, now(), 'seed', now(), 'seed', '52837194664', '2030-03-31', 37.0,
  '0cd342e7-64bb-42a2-be08-82796ef61495', '5647b3d4-f42f-4905-a617-896afa8dccef');

INSERT INTO equipament_table (id, active, created_at, created_by, updated_at, updated_by, plate, renavam, model,
                              number_axles, maximum_capacity, status, company_id) VALUES
 ('c4bef783-d583-4c1f-9142-4bf6ee121596', true, now(), 'seed', now(), 'seed', 'CST4B19', '83920174561', 'Volvo FH 540',
  3, 32000, 'ATIVO', '5647b3d4-f42f-4905-a617-896afa8dccef'),
 ('52261194-1f73-4e3f-a4df-2bc6a9bf4f00', true, now(), 'seed', now(), 'seed', 'RCH7D22', '72619483052', 'Scania R 450',
  3, 30000, 'ATIVO', '5647b3d4-f42f-4905-a617-896afa8dccef'),
 ('a4fb1bf2-29f7-4aa5-ac93-1a24a6dec510', true, now(), 'seed', now(), 'seed', 'ALM3F88', '61938274015',
  'Mercedes-Benz Actros 2651', 3, 34000, 'ATIVO', '5647b3d4-f42f-4905-a617-896afa8dccef');

-- Consumo médio (km/L) é dado de demonstração; o CO₂ é calculado a partir dele (ver EmissionFactors)
INSERT INTO tractor_table (id, type_fuel, cost_per_kilometer, co2_per_kilometer, km_per_liter, vehicle_type, cargo_volume) VALUES
 ('c4bef783-d583-4c1f-9142-4bf6ee121596', 'DIESEL', 4.20, NULL, 2.4, 'CAVALO_MECANICO', NULL),
 ('52261194-1f73-4e3f-a4df-2bc6a9bf4f00', 'DIESEL', 4.05, NULL, 2.6, 'CAVALO_MECANICO', NULL),
 ('a4fb1bf2-29f7-4aa5-ac93-1a24a6dec510', 'DIESEL', 4.35, NULL, 2.3, 'CAVALO_MECANICO', NULL);

INSERT INTO equipament_group_table (id, active, created_at, created_by, updated_at, updated_by, observation,
                                    equipament1_id, status, company_id) VALUES
 ('5c0fd084-b358-4aae-aed0-2282963c26ca', true, now(), 'seed', now(), 'seed', 'Costa Express — Volvo FH 540',
  'c4bef783-d583-4c1f-9142-4bf6ee121596', 'DISPONIVEL', '5647b3d4-f42f-4905-a617-896afa8dccef'),
 ('820c342d-3430-457b-8d4d-2f1fcbed8ff1', true, now(), 'seed', now(), 'seed', 'Rocha Translog — Scania R 450',
  '52261194-1f73-4e3f-a4df-2bc6a9bf4f00', 'DISPONIVEL', '5647b3d4-f42f-4905-a617-896afa8dccef'),
 ('c432a029-2c73-43fe-8ae8-b98fee04a8bb', true, now(), 'seed', now(), 'seed', 'Almeida Carga Viva — Actros 2651',
  'a4fb1bf2-29f7-4aa5-ac93-1a24a6dec510', 'DISPONIVEL', '5647b3d4-f42f-4905-a617-896afa8dccef');

INSERT INTO work_schedule (id, active, created_at, created_by, updated_at, updated_by, schedule_date, status,
                           driver_id, equipament_group_id, start_workday, end_workday, break_duration_minutes,
                           earliest_break_time, latest_break_time, max_driving_hours_without_break,
                           min_driving_break_minutes, company_id) VALUES
 ('a6d6156e-39f8-4156-a208-d32e0a578def', true, now(), 'seed', now(), 'seed', '2026-11-10', 'ATIVO',
  '7769e02b-87d9-475f-b400-6c4df77d14aa', '5c0fd084-b358-4aae-aed0-2282963c26ca', '08:00', '18:00', 60, '11:30',
  '13:30', 4, 30, '5647b3d4-f42f-4905-a617-896afa8dccef'),
 ('0b71d952-5754-48ca-982f-ebf5dc53d0ae', true, now(), 'seed', now(), 'seed', '2026-11-10', 'ATIVO',
  'bc936c40-f0c2-45a7-bb69-721a10888cd4', '820c342d-3430-457b-8d4d-2f1fcbed8ff1', '07:30', '17:30', 60, '11:30',
  '13:30', 4, 30, '5647b3d4-f42f-4905-a617-896afa8dccef'),
 ('3197e9b3-686a-41f0-8905-c60768db49be', true, now(), 'seed', now(), 'seed', '2026-11-10', 'ATIVO',
  '63a2dc1a-4362-453c-a753-20c42e30bb4a', 'c432a029-2c73-43fe-8ae8-b98fee04a8bb', '08:00', '18:00', 60, '11:30',
  '13:30', 4, 30, '5647b3d4-f42f-4905-a617-896afa8dccef');

COMMIT;
