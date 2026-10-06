-- ==============================================================================
-- GOLOG TMS - SEED DE DEMONSTRAÇÃO E TESTES MULTI-TENANT
-- ==============================================================================

-- 1. LIMPEZA SEGURA DAS TABELAS OPERACIONAIS
TRUNCATE TABLE 
    occurrence_table,
    route_stop_table,
    transport_table,
    shipment_demand_table,
    shipment_table,
    work_schedule,
    equipament_group_type_transport_table,
    equipament_group_table,
    driver_table,
    equipament_capacity_table,
    tractor_table,
    trailer_table,
    equipament_table,
    optimization_profile,
    auth_log_table
CASCADE;

-- Limpeza de usuarios de teste (preserva super admins se houver, ou recria todos padronizados)
DELETE FROM user_table WHERE email NOT IN ('master@golog.com', 'admin@admin.com');

-- 2. GARANTIR TENANTS PRINCIPAIS
-- GoLog Master (ID: 7f564f96-d90f-42cc-beb2-e37cf63a324d)
INSERT INTO company_table (id, legal_name, cnpj_cpf, phone_number, email, is_cliente, company_type, is_master, parent_company_id, active, created_at)
VALUES ('7f564f96-d90f-42cc-beb2-e37cf63a324d', 'GoLog Transportes & Tecnologia', '49018326000195', '(19)983282551', 'master@golog.com', false, 'TENANT_MASTER', true, NULL, true, NOW())
ON CONFLICT (id) DO UPDATE SET 
    legal_name = 'GoLog Transportes & Tecnologia',
    company_type = 'TENANT_MASTER',
    is_master = true,
    active = true;

-- Nestlé Brasil Matriz (ID: d9d7b435-c256-405b-877c-848f4a22e22a)
INSERT INTO company_table (id, legal_name, cnpj_cpf, phone_number, email, is_cliente, company_type, is_master, parent_company_id, active, created_at)
VALUES ('d9d7b435-c256-405b-877c-848f4a22e22a', 'Nestlé Brasil Matriz', '60409075000667', '(19)35431100', 'admin@admin.com', false, 'TENANT_HEADQUARTER', false, NULL, true, NOW())
ON CONFLICT (id) DO UPDATE SET 
    legal_name = 'Nestlé Brasil Matriz',
    company_type = 'TENANT_HEADQUARTER',
    is_master = false,
    parent_company_id = NULL,
    active = true;

-- Nestlé Filial Ribeirão Preto (ID: d151ac55-962a-4e91-90a2-f2422df19266)
INSERT INTO company_table (id, legal_name, cnpj_cpf, phone_number, email, is_cliente, company_type, is_master, parent_company_id, active, created_at)
VALUES ('d151ac55-962a-4e91-90a2-f2422df19266', 'Nestlé Filial Ribeirão Preto', '60409075000748', '(16)35431100', 'admin.rp@nestle.com.br', false, 'TENANT_BRANCH', false, 'd9d7b435-c256-405b-877c-848f4a22e22a', true, NOW())
ON CONFLICT (id) DO UPDATE SET 
    legal_name = 'Nestlé Filial Ribeirão Preto',
    company_type = 'TENANT_BRANCH',
    is_master = false,
    parent_company_id = 'd9d7b435-c256-405b-877c-848f4a22e22a',
    active = true;

-- Lactalis Brasil Matriz (ID: af29a1ab-407a-497d-b721-f3b93450d9eb)
INSERT INTO company_table (id, legal_name, cnpj_cpf, phone_number, email, is_cliente, company_type, is_master, parent_company_id, active, created_at)
VALUES ('af29a1ab-407a-497d-b721-f3b93450d9eb', 'Lactalis Brasil Laticínios', '05300331003267', '(19)35434500', 'admin@lactalis.com.br', false, 'TENANT_HEADQUARTER', false, NULL, true, NOW())
ON CONFLICT (id) DO UPDATE SET 
    legal_name = 'Lactalis Brasil Laticínios',
    company_type = 'TENANT_HEADQUARTER',
    is_master = false,
    parent_company_id = NULL,
    active = true;

-- Parceiros Comerciais (Clientes e Fornecedores)
INSERT INTO company_table (id, legal_name, cnpj_cpf, phone_number, email, is_cliente, company_type, is_master, parent_company_id, active, created_at)
VALUES 
    ('c1111111-1111-1111-1111-111111111111', 'Pão de Açúcar CD São Paulo', '47508411000156', '(11)38860533', 'cd.sp@paodeacucar.com.br', true, 'CLIENT', false, NULL, true, NOW()),
    ('c2222222-2222-2222-2222-222222222222', 'Carrefour CD Campinas', '45543915000181', '(19)37544000', 'logistica@carrefour.com.br', true, 'CLIENT', false, NULL, true, NOW()),
    ('c3333333-3333-3333-3333-333333333333', 'Ambev Fábrica Jaguariúna', '56998982000128', '(19)38679000', 'distribuicao@ambev.com.br', false, 'SUPPLIER', false, NULL, true, NOW())
ON CONFLICT (id) DO UPDATE SET 
    legal_name = EXCLUDED.legal_name,
    company_type = EXCLUDED.company_type,
    active = true;

-- 3. ENDEREÇOS VINCULADOS AOS TENANTS
INSERT INTO address_table (id, company_id, street, number, district, city, state, country, cep, latitude, longitude, active, created_at)
VALUES
    -- Endereço GoLog Master
    ('a0000000-0000-0000-0000-000000000001', '7f564f96-d90f-42cc-beb2-e37cf63a324d', 'Av. Paulista', '1000', 'Bela Vista', 'São Paulo', 'SP', 'Brasil', '01310100', '-23.561684', '-46.655981', true, NOW()),
    -- Endereços Nestlé Matriz
    ('a1111111-1111-1111-1111-111111111111', 'd9d7b435-c256-405b-877c-848f4a22e22a', 'Rua Nestlé', '100', 'Distrito Industrial', 'Araras', 'SP', 'Brasil', '13600000', '-22.357200', '-47.384200', true, NOW()),
    ('a1111111-1111-1111-1111-111111111112', 'd9d7b435-c256-405b-877c-848f4a22e22a', 'Av. Nações Unidas', '12551', 'Brooklin', 'São Paulo', 'SP', 'Brasil', '04578903', '-23.608900', '-46.696800', true, NOW()),
    -- Endereço Nestlé Filial Ribeirão
    ('a2222222-2222-2222-2222-222222222221', 'd151ac55-962a-4e91-90a2-f2422df19266', 'Av. Independência', '500', 'Jardim Sumaré', 'Ribeirão Preto', 'SP', 'Brasil', '14096000', '-21.176700', '-47.810800', true, NOW()),
    -- Endereço Lactalis Matriz
    ('a3333333-3333-3333-3333-333333333331', 'af29a1ab-407a-497d-b721-f3b93450d9eb', 'Rodovia Anhanguera', 'KM 165', 'Zona Rural', 'Araras', 'SP', 'Brasil', '13600970', '-22.340000', '-47.360000', true, NOW())
ON CONFLICT (id) DO UPDATE SET company_id = EXCLUDED.company_id, active = true;

UPDATE company_table SET address_id = 'a0000000-0000-0000-0000-000000000001' WHERE id = '7f564f96-d90f-42cc-beb2-e37cf63a324d';
UPDATE company_table SET address_id = 'a1111111-1111-1111-1111-111111111111' WHERE id = 'd9d7b435-c256-405b-877c-848f4a22e22a';
UPDATE company_table SET address_id = 'a2222222-2222-2222-2222-222222222221' WHERE id = 'd151ac55-962a-4e91-90a2-f2422df19266';
UPDATE company_table SET address_id = 'a3333333-3333-3333-3333-333333333331' WHERE id = 'af29a1ab-407a-497d-b721-f3b93450d9eb';

-- 4. USUÁRIOS DO SISTEMA (Senha para todos: Admin@123)
-- Hash BCrypt: $2a$10$ArcCT.dC00YRsOFROZOffedZWEJgQpmralIVFz47cAzOz3LBauigi
INSERT INTO user_table (id, company_id, name, email, password, cpf, user_profile, active, created_at)
VALUES
    -- Super Admin GoLog Master
    ('00000000-0000-0000-0000-000000000001', '7f564f96-d90f-42cc-beb2-e37cf63a324d', 'GoLog Super Admin', 'master@golog.com', '$2a$10$ArcCT.dC00YRsOFROZOffedZWEJgQpmralIVFz47cAzOz3LBauigi', '11111111111', 'ADMIN', true, NOW()),
    
    -- Nestlé Matriz: Admin, Operador e Motoristas
    ('d9d7b435-0000-0000-0000-000000000001', 'd9d7b435-c256-405b-877c-848f4a22e22a', 'Admin Nestlé Matriz', 'admin@admin.com', '$2a$10$ArcCT.dC00YRsOFROZOffedZWEJgQpmralIVFz47cAzOz3LBauigi', '22222222222', 'ADMIN', true, NOW()),
    ('d9d7b435-0000-0000-0000-000000000002', 'd9d7b435-c256-405b-877c-848f4a22e22a', 'Carlos Operador Nestlé', 'operador@nestle.com', '$2a$10$ArcCT.dC00YRsOFROZOffedZWEJgQpmralIVFz47cAzOz3LBauigi', '33333333333', 'OPERATOR', true, NOW()),
    ('d9d7b435-0000-0000-0000-000000000003', 'd9d7b435-c256-405b-877c-848f4a22e22a', 'Jonathan Alves (Motorista)', 'jonathan.motorista@nestle.com', '$2a$10$ArcCT.dC00YRsOFROZOffedZWEJgQpmralIVFz47cAzOz3LBauigi', '44444444444', 'DRIVER', true, NOW()),
    ('d9d7b435-0000-0000-0000-000000000004', 'd9d7b435-c256-405b-877c-848f4a22e22a', 'Fernando Silva (Motorista)', 'fernando.motorista@nestle.com', '$2a$10$ArcCT.dC00YRsOFROZOffedZWEJgQpmralIVFz47cAzOz3LBauigi', '55555555555', 'DRIVER', true, NOW()),

    -- Nestlé Filial Ribeirão Preto: Admin e Motorista
    ('d151ac55-0000-0000-0000-000000000001', 'd151ac55-962a-4e91-90a2-f2422df19266', 'Admin Filial Ribeirão', 'admin.rp@nestle.com.br', '$2a$10$ArcCT.dC00YRsOFROZOffedZWEJgQpmralIVFz47cAzOz3LBauigi', '66666666666', 'ADMIN', true, NOW()),
    ('d151ac55-0000-0000-0000-000000000002', 'd151ac55-962a-4e91-90a2-f2422df19266', 'Ricardo Ribeirão (Motorista)', 'ricardo.rp@nestle.com.br', '$2a$10$ArcCT.dC00YRsOFROZOffedZWEJgQpmralIVFz47cAzOz3LBauigi', '77777777777', 'DRIVER', true, NOW()),

    -- Lactalis Matriz: Admin e Motorista
    ('af29a1ab-0000-0000-0000-000000000001', 'af29a1ab-407a-497d-b721-f3b93450d9eb', 'Admin Lactalis Matriz', 'admin@lactalis.com.br', '$2a$10$ArcCT.dC00YRsOFROZOffedZWEJgQpmralIVFz47cAzOz3LBauigi', '88888888888', 'ADMIN', true, NOW()),
    ('af29a1ab-0000-0000-0000-000000000002', 'af29a1ab-407a-497d-b721-f3b93450d9eb', 'Marcos Laticínios (Motorista)', 'marcos.motorista@lactalis.com.br', '$2a$10$ArcCT.dC00YRsOFROZOffedZWEJgQpmralIVFz47cAzOz3LBauigi', '99999999999', 'DRIVER', true, NOW())
ON CONFLICT (id) DO UPDATE SET 
    company_id = EXCLUDED.company_id,
    password = EXCLUDED.password,
    user_profile = EXCLUDED.user_profile,
    active = true;

-- 5. MOTORISTAS (driver_table)
INSERT INTO driver_table (id, user_id, company_id, cnh_number, cnh_expiration, cost_per_hour, active, created_at)
VALUES
    -- Motoristas Nestlé Matriz
    ('mot00001-0000-0000-0000-000000000001', 'd9d7b435-0000-0000-0000-000000000003', 'd9d7b435-c256-405b-877c-848f4a22e22a', '12345678901', '2028-12-31', 45.00, true, NOW()),
    ('mot00001-0000-0000-0000-000000000002', 'd9d7b435-0000-0000-0000-000000000004', 'd9d7b435-c256-405b-877c-848f4a22e22a', '12345678902', '2027-08-15', 50.00, true, NOW()),
    
    -- Motorista Nestlé Filial RP
    ('mot00002-0000-0000-0000-000000000001', 'd151ac55-0000-0000-0000-000000000002', 'd151ac55-962a-4e91-90a2-f2422df19266', '23456789012', '2029-05-20', 42.00, true, NOW()),

    -- Motorista Lactalis
    ('mot00003-0000-0000-0000-000000000001', 'af29a1ab-0000-0000-0000-000000000002', 'af29a1ab-407a-497d-b721-f3b93450d9eb', '34567890123', '2028-02-10', 48.00, true, NOW())
ON CONFLICT (id) DO UPDATE SET company_id = EXCLUDED.company_id, active = true;

-- 6. FROTA DE VEÍCULOS (equipament_table, tractor_table, trailer_table)
-- Nestlé Matriz: 2 Cavalos (Tractors) e 2 Carretas (Trailers)
INSERT INTO equipament_table (id, company_id, plate, renavam, model, number_axles, maximum_capacity, status, active, created_at)
VALUES
    ('eq-trac-01-0000-0000-0000-000000000001', 'd9d7b435-c256-405b-877c-848f4a22e22a', 'BRA2E19', '11223344551', 'Scania R450 6x2', 3, 25000.0, 'DISPONIVEL', true, NOW()),
    ('eq-trac-02-0000-0000-0000-000000000002', 'd9d7b435-c256-405b-877c-848f4a22e22a', 'NES1A23', '11223344552', 'Volvo FH 540 6x4', 3, 30000.0, 'DISPONIVEL', true, NOW()),
    ('eq-trai-01-0000-0000-0000-000000000001', 'd9d7b435-c256-405b-877c-848f4a22e22a', 'CAR1A11', '99887766551', 'Semirreboque Sider Randon', 3, 27000.0, 'DISPONIVEL', true, NOW()),
    ('eq-trai-02-0000-0000-0000-000000000002', 'd9d7b435-c256-405b-877c-848f4a22e22a', 'CAR2B22', '99887766552', 'Semirreboque Baú Facchini', 3, 26000.0, 'DISPONIVEL', true, NOW()),
    
    -- Nestlé Filial RP: 1 Cavalo e 1 Carreta
    ('eq-trac-rp-0000-0000-0000-000000000001', 'd151ac55-962a-4e91-90a2-f2422df19266', 'RPB9X99', '22334455661', 'Volvo VM 330 8x2', 4, 20000.0, 'DISPONIVEL', true, NOW()),
    ('eq-trai-rp-0000-0000-0000-000000000001', 'd151ac55-962a-4e91-90a2-f2422df19266', 'RPC8Y88', '33445566771', 'Carreta Graneleiro Guerra', 3, 22000.0, 'DISPONIVEL', true, NOW()),

    -- Lactalis: 1 Cavalo e 1 Carreta Frigorífica
    ('eq-trac-lac0000-0000-0000-000000000001', 'af29a1ab-407a-497d-b721-f3b93450d9eb', 'LAC3Z33', '44556677881', 'Mercedes-Benz Actros 2651', 3, 28000.0, 'DISPONIVEL', true, NOW()),
    ('eq-trai-lac0000-0000-0000-000000000001', 'af29a1ab-407a-497d-b721-f3b93450d9eb', 'LAC4W44', '55667788991', 'Carreta Frigorífica Ibiporã', 3, 24000.0, 'DISPONIVEL', true, NOW())
ON CONFLICT (id) DO UPDATE SET company_id = EXCLUDED.company_id, active = true;

INSERT INTO tractor_table (id, km_per_liter, type_fuel, co2_per_kilometer)
VALUES
    ('eq-trac-01-0000-0000-0000-000000000001', 2.8, 'DIESEL', 0.95),
    ('eq-trac-02-0000-0000-0000-000000000002', 2.5, 'DIESEL', 1.07),
    ('eq-trac-rp-0000-0000-0000-000000000001', 3.2, 'DIESEL', 0.84),
    ('eq-trac-lac0000-0000-0000-000000000001', 2.6, 'DIESEL', 1.03)
ON CONFLICT (id) DO NOTHING;

INSERT INTO trailer_table (id)
VALUES
    ('eq-trai-01-0000-0000-0000-000000000001'),
    ('eq-trai-02-0000-0000-0000-000000000002'),
    ('eq-trai-rp-0000-0000-0000-000000000001'),
    ('eq-trai-lac0000-0000-0000-000000000001')
ON CONFLICT (id) DO NOTHING;

-- Capacidades
INSERT INTO equipament_capacity_table (id, equipament_id, type, maximum_capacity, created_at)
VALUES
    ('cap00001-0000-0000-0000-000000000001', 'eq-trai-01-0000-0000-0000-000000000001', 'PESO', 27000.0, NOW()),
    ('cap00001-0000-0000-0000-000000000002', 'eq-trai-01-0000-0000-0000-000000000001', 'VOLUME', 90.0, NOW()),
    ('cap00002-0000-0000-0000-000000000001', 'eq-trai-lac0000-0000-0000-000000000001', 'PESO', 24000.0, NOW()),
    ('cap00002-0000-0000-0000-000000000002', 'eq-trai-lac0000-0000-0000-000000000001', 'VOLUME', 80.0, NOW())
ON CONFLICT (id) DO NOTHING;

-- 7. CONJUNTOS DE EQUIPAMENTOS (equipament_group_table)
INSERT INTO equipament_group_table (id, company_id, equipament1_id, equipament2_id, equipament3_id, observation, status, active, created_at)
VALUES
    -- Conjunto 1 Nestlé Matriz (Cavalo Scania + Sider)
    ('grp00001-0000-0000-0000-000000000001', 'd9d7b435-c256-405b-877c-848f4a22e22a', 'eq-trac-01-0000-0000-0000-000000000001', 'eq-trai-01-0000-0000-0000-000000000001', NULL, 'Conjunto Rota SP Interior', 'DISPONIVEL', true, NOW()),
    -- Conjunto 2 Nestlé Matriz (Cavalo Volvo + Baú)
    ('grp00001-0000-0000-0000-000000000002', 'd9d7b435-c256-405b-877c-848f4a22e22a', 'eq-trac-02-0000-0000-0000-000000000002', 'eq-trai-02-0000-0000-0000-000000000002', NULL, 'Conjunto Transferência Pesada', 'DISPONIVEL', true, NOW()),
    -- Conjunto 3 Nestlé Filial RP
    ('grp00002-0000-0000-0000-000000000001', 'd151ac55-962a-4e91-90a2-f2422df19266', 'eq-trac-rp-0000-0000-0000-000000000001', 'eq-trai-rp-0000-0000-0000-000000000001', NULL, 'Conjunto Graneleiro Regional RP', 'DISPONIVEL', true, NOW()),
    -- Conjunto 4 Lactalis
    ('grp00003-0000-0000-0000-000000000001', 'af29a1ab-407a-497d-b721-f3b93450d9eb', 'eq-trac-lac0000-0000-0000-000000000001', 'eq-trai-lac0000-0000-0000-000000000001', NULL, 'Conjunto Refrigerado Lácteos', 'DISPONIVEL', true, NOW())
ON CONFLICT (id) DO UPDATE SET company_id = EXCLUDED.company_id, active = true;

-- 8. ESCALAS DE TRABALHO (work_schedule)
INSERT INTO work_schedule (id, company_id, driver_id, equipament_group_id, schedule_date, start_workday, end_workday, status, active, created_at)
VALUES
    -- Escala Nestlé Matriz (Jonathan + Conjunto 1)
    ('sch00001-0000-0000-0000-000000000001', 'd9d7b435-c256-405b-877c-848f4a22e22a', 'mot00001-0000-0000-0000-000000000001', 'grp00001-0000-0000-0000-000000000001', CURRENT_DATE, '07:00:00', '17:00:00', 'DISPONIVEL', true, NOW()),
    -- Escala Nestlé Filial RP (Ricardo + Conjunto RP)
    ('sch00002-0000-0000-0000-000000000001', 'd151ac55-962a-4e91-90a2-f2422df19266', 'mot00002-0000-0000-0000-000000000001', 'grp00002-0000-0000-0000-000000000001', CURRENT_DATE, '08:00:00', '18:00:00', 'DISPONIVEL', true, NOW()),
    -- Escala Lactalis (Marcos + Conjunto Refrigerado)
    ('sch00003-0000-0000-0000-000000000001', 'af29a1ab-407a-497d-b721-f3b93450d9eb', 'mot00003-0000-0000-0000-000000000001', 'grp00003-0000-0000-0000-000000000001', CURRENT_DATE, '06:00:00', '16:00:00', 'DISPONIVEL', true, NOW())
ON CONFLICT (id) DO UPDATE SET company_id = EXCLUDED.company_id, active = true;

-- 9. PERFIS DE OTIMIZAÇÃO (optimization_profile)
INSERT INTO optimization_profile (id, company_id, name, description, is_default, cost_per_kilometer, cost_per_hour, max_distance_meters, max_time_seconds, max_capacity_weight, max_capacity_volume, enable_traffic, optimization_objective, active, created_at)
VALUES
    -- Perfil Padrão Nestlé Matriz
    ('opt00001-0000-0000-0000-000000000001', 'd9d7b435-c256-405b-877c-848f4a22e22a', 'Perfil Padrão Nestlé - Carga Seca', 'Perfil com foco em menor custo e janela de entrega comercial', true, 4.50, 60.00, 500000, 36000, 27000.0, 90.0, true, 'MINIMIZE_COST', true, NOW()),
    -- Perfil Lactalis (Refrigerado)
    ('opt00002-0000-0000-0000-000000000001', 'af29a1ab-407a-497d-b721-f3b93450d9eb', 'Perfil Lactalis - Cadeia Fria', 'Foco em tempo rápido de percurso para produtos perecíveis', true, 5.20, 75.00, 400000, 28800, 24000.0, 80.0, true, 'MINIMIZE_TIME', true, NOW())
ON CONFLICT (id) DO UPDATE SET active = true;

-- 10. REMESSAS E CARGAS (shipment_table)
-- Tipos de transporte e carga padrão existentes
DO $$
DECLARE
    v_type_transp UUID;
    v_type_ship UUID;
BEGIN
    SELECT id INTO v_type_transp FROM type_transport_table LIMIT 1;
    SELECT id INTO v_type_ship FROM shipment_type LIMIT 1;

    -- Remessas Nestlé Matriz (Entregas para Pão de Açúcar e Carrefour)
    INSERT INTO shipment_table (id, company_id, responsible_id, shipment_customer_id, shipment_address_id, shipment_type_id, type_transport_id, type_operation, weight, volume, schedulind, status, active, created_at)
    VALUES
        ('shp00001-0000-0000-0000-000000000001', 'd9d7b435-c256-405b-877c-848f4a22e22a', 'd9d7b435-0000-0000-0000-000000000001', 'c1111111-1111-1111-1111-111111111111', 'a1111111-1111-1111-1111-111111111112', v_type_ship, v_type_transp, 'ENTREGA', 5500.0, 18.0, NOW() + INTERVAL '1 day', 'PENDENTE', true, NOW()),
        ('shp00001-0000-0000-0000-000000000002', 'd9d7b435-c256-405b-877c-848f4a22e22a', 'd9d7b435-0000-0000-0000-000000000001', 'c2222222-2222-2222-2222-222222222222', 'a1111111-1111-1111-1111-111111111112', v_type_ship, v_type_transp, 'ENTREGA', 7200.0, 24.0, NOW() + INTERVAL '1 day', 'PENDENTE', true, NOW()),
        
    -- Remessa Lactalis (Entrega de Laticínios)
        ('shp00002-0000-0000-0000-000000000001', 'af29a1ab-407a-497d-b721-f3b93450d9eb', 'af29a1ab-0000-0000-0000-000000000001', 'c1111111-1111-1111-1111-111111111111', 'a0000000-0000-0000-0000-000000000001', v_type_ship, v_type_transp, 'ENTREGA', 4300.0, 15.0, NOW() + INTERVAL '1 day', 'PENDENTE', true, NOW()),

    -- Remessa Nestlé Filial RP
        ('shp00003-0000-0000-0000-000000000001', 'd151ac55-962a-4e91-90a2-f2422df19266', 'd151ac55-0000-0000-0000-000000000001', 'c2222222-2222-2222-2222-222222222222', 'a2222222-2222-2222-2222-222222222221', v_type_ship, v_type_transp, 'ENTREGA', 3100.0, 11.0, NOW() + INTERVAL '2 days', 'PENDENTE', true, NOW())
    ON CONFLICT (id) DO UPDATE SET company_id = EXCLUDED.company_id, active = true;

    -- Transportes / Viagens
    INSERT INTO transport_table (id, transporter_id, driver_id, equipament_group_id, route_planned, shipment_quantity, calculed_distance, time_stopped_calculed, travel_duration, total_cost_calculed, active, created_at)
    VALUES
        ('trp00001-0000-0000-0000-000000000001', 'd9d7b435-c256-405b-877c-848f4a22e22a', 'mot00001-0000-0000-0000-000000000001', 'grp00001-0000-0000-0000-000000000001', 'Araras SP -> São Paulo Capital (CD Pão de Açúcar)', 1, 172000, 1800, 7200, 890.50, true, NOW()),
        ('trp00002-0000-0000-0000-000000000001', 'af29a1ab-407a-497d-b721-f3b93450d9eb', 'mot00003-0000-0000-0000-000000000001', 'grp00003-0000-0000-0000-000000000001', 'Araras SP -> Campinas SP (CD Carrefour)', 1, 98000, 1200, 4500, 540.00, true, NOW())
    ON CONFLICT (id) DO UPDATE SET transporter_id = EXCLUDED.transporter_id, active = true;

    -- Ocorrências de Teste
    INSERT INTO occurrence_table (id, company_id, delivery_id, transport_id, sender_id, type, description, date_time, active, created_at)
    VALUES
        ('ocr00001-0000-0000-0000-000000000001', 'd9d7b435-c256-405b-877c-848f4a22e22a', 'shp00001-0000-0000-0000-000000000001', 'trp00001-0000-0000-0000-000000000001', 'd9d7b435-0000-0000-0000-000000000001', 'Inicio', 'Início de rota na fábrica de Araras', NOW(), true, NOW()),
        ('ocr00002-0000-0000-0000-000000000001', 'af29a1ab-407a-497d-b721-f3b93450d9eb', 'shp00002-0000-0000-0000-000000000001', 'trp00002-0000-0000-0000-000000000001', 'af29a1ab-0000-0000-0000-000000000001', 'Inicio', 'Saída do armazém refrigerado', NOW(), true, NOW())
    ON CONFLICT (id) DO UPDATE SET company_id = EXCLUDED.company_id, active = true;

END $$;
