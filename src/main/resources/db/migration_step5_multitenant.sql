-- =========================================================================
-- MIGRAÇÃO DE MULTI-TENANT ROBUSTO PARA GOLOG TMS
-- Suporte a N tenants, Matriz/Filiais, Isolamento de Dados e Tenant Master (GoLog)
-- =========================================================================

-- 1. Adequação da tabela de empresas (company_table)
ALTER TABLE company_table ADD COLUMN IF NOT EXISTS company_type VARCHAR(50) DEFAULT 'CLIENT';
ALTER TABLE company_table ADD COLUMN IF NOT EXISTS is_master BOOLEAN DEFAULT false;
ALTER TABLE company_table ADD COLUMN IF NOT EXISTS parent_company_id UUID;

-- Constraint de chave estrangeira auto-referencial (Filial -> Matriz)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_company_parent'
    ) THEN
        ALTER TABLE company_table 
        ADD CONSTRAINT fk_company_parent 
        FOREIGN KEY (parent_company_id) REFERENCES company_table(id) ON DELETE SET NULL;
    END IF;
END $$;

-- Atualizar classificação dos registros existentes
UPDATE company_table 
SET is_master = true, company_type = 'TENANT_MASTER', is_cliente = false 
WHERE id = '7f564f96-d90f-42cc-beb2-e37cf63a324d' OR legal_name ILIKE '%golog%';

UPDATE company_table 
SET is_master = false, company_type = 'TENANT_HEADQUARTER', is_cliente = false 
WHERE id IN ('d9d7b435-c256-405b-877c-848f4a22e22a', 'af29a1ab-407a-497d-b721-f3b93450d9eb');

UPDATE company_table 
SET company_type = 'CLIENT', is_cliente = true, is_master = false 
WHERE company_type IS NULL OR (company_type NOT IN ('TENANT_MASTER', 'TENANT_HEADQUARTER', 'TENANT_BRANCH'));

-- 2. Garantir usuário Administrador Master no tenant GoLog
INSERT INTO user_table (
    id, active, created_at, created_by, updated_at, updated_by, 
    cpf, email, name, password, user_profile, company_id
)
SELECT 
    '00000000-0000-0000-0000-000000000001'::uuid, 
    true, NOW(), 'system', NOW(), 'system',
    '000.000.000-01', 'master@golog.com', 'GoLog Super Admin',
    '$2a$10$ArcCT.dC00YRsOFROZOffedZWEJgQpmralIVFz47cAzOz3LBauigi', -- Admin@123
    'ADMIN', '7f564f96-d90f-42cc-beb2-e37cf63a324d'::uuid
WHERE NOT EXISTS (
    SELECT 1 FROM user_table WHERE email = 'master@golog.com'
);

-- 3. EquipamentGroup (Conjuntos de Equipamentos) -> Vínculo obrigatório com Company
ALTER TABLE equipament_group_table ADD COLUMN IF NOT EXISTS company_id UUID;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_equipament_group_company') THEN
        ALTER TABLE equipament_group_table 
        ADD CONSTRAINT fk_equipament_group_company 
        FOREIGN KEY (company_id) REFERENCES company_table(id) ON DELETE CASCADE;
    END IF;
END $$;

UPDATE equipament_group_table eg 
SET company_id = (SELECT e.company_id FROM equipament_table e WHERE e.id = eg.equipament1_id)
WHERE eg.company_id IS NULL;

-- 4. WorkSchedule (Escalas de Trabalho) -> Vínculo obrigatório com Company
ALTER TABLE work_schedule ADD COLUMN IF NOT EXISTS company_id UUID;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_work_schedule_company') THEN
        ALTER TABLE work_schedule 
        ADD CONSTRAINT fk_work_schedule_company 
        FOREIGN KEY (company_id) REFERENCES company_table(id) ON DELETE CASCADE;
    END IF;
END $$;

UPDATE work_schedule ws 
SET company_id = (SELECT eg.company_id FROM equipament_group_table eg WHERE eg.id = ws.equipament_group_id)
WHERE ws.company_id IS NULL;

-- 5. Shipment (Remessas / Cargas) -> Vínculo obrigatório com Tenant Operador
ALTER TABLE shipment_table ADD COLUMN IF NOT EXISTS company_id UUID;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_shipment_company') THEN
        ALTER TABLE shipment_table 
        ADD CONSTRAINT fk_shipment_company 
        FOREIGN KEY (company_id) REFERENCES company_table(id) ON DELETE CASCADE;
    END IF;
END $$;

UPDATE shipment_table s 
SET company_id = (SELECT u.company_id FROM user_table u WHERE u.id = s.responsible_id)
WHERE s.company_id IS NULL;

-- Fallback para remessas orfãs (associa ao tenant Nestlé ou GoLog)
UPDATE shipment_table 
SET company_id = 'd9d7b435-c256-405b-877c-848f4a22e22a' 
WHERE company_id IS NULL;

-- 6. Occurrence (Ocorrências) -> Vínculo obrigatório com Company
ALTER TABLE occurrence_table ADD COLUMN IF NOT EXISTS company_id UUID;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_occurrence_company') THEN
        ALTER TABLE occurrence_table 
        ADD CONSTRAINT fk_occurrence_company 
        FOREIGN KEY (company_id) REFERENCES company_table(id) ON DELETE CASCADE;
    END IF;
END $$;

UPDATE occurrence_table occ 
SET company_id = (SELECT t.transporter_id FROM transport_table t WHERE t.id = occ.transport_id)
WHERE occ.company_id IS NULL;

-- 7. TypeTransport e ShipmentType (Tipos de transporte e carga) -> Tenant opcional (null = compartilhado)
ALTER TABLE type_transport_table ADD COLUMN IF NOT EXISTS company_id UUID;
ALTER TABLE shipment_type ADD COLUMN IF NOT EXISTS company_id UUID;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_type_transport_company') THEN
        ALTER TABLE type_transport_table 
        ADD CONSTRAINT fk_type_transport_company 
        FOREIGN KEY (company_id) REFERENCES company_table(id) ON DELETE CASCADE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_shipment_type_company') THEN
        ALTER TABLE shipment_type 
        ADD CONSTRAINT fk_shipment_type_company 
        FOREIGN KEY (company_id) REFERENCES company_table(id) ON DELETE CASCADE;
    END IF;
END $$;
