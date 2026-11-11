-- =========================================================================
-- MIGRAÇÃO STEP 8: Propriedade de tenant consistente (idempotente)
--
-- Contexto: até aqui clientes/fornecedores eram globais (visíveis a todos os tenants), vários
-- registros operacionais ficaram com company_id nulo e motoristas/veículos foram gravados em empresas
-- do tipo CLIENT (o formulário pré-selecionava o primeiro item da lista). Com o isolamento estrito
-- introduzido no TenantGuard, esses registros sumiriam das listas — esta migração os reatribui ao
-- tenant onde de fato operam.
--
-- Regra de fallback: o tenant Master (is_master = true) é o dono dos registros sem dono evidente.
-- Pode ser executada várias vezes sem efeito colateral.
-- =========================================================================

-- 0. Coluna nova (o Hibernate também a cria com ddl-auto=update)
ALTER TABLE company_table ADD COLUMN IF NOT EXISTS owner_tenant_id UUID;
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_company_owner_tenant') THEN
        ALTER TABLE company_table
            ADD CONSTRAINT fk_company_owner_tenant FOREIGN KEY (owner_tenant_id) REFERENCES company_table(id);
    END IF;
END $$;

-- 1. Clientes/fornecedores: dono = tenant que mais os usa em remessas; senão o Master
UPDATE company_table c
SET owner_tenant_id = sub.tenant_id
FROM (
    SELECT DISTINCT ON (s.shipment_customer_id) s.shipment_customer_id AS customer_id, s.company_id AS tenant_id
    FROM shipment_table s
    WHERE s.company_id IS NOT NULL
    GROUP BY s.shipment_customer_id, s.company_id
    ORDER BY s.shipment_customer_id, COUNT(*) DESC
) sub
WHERE c.id = sub.customer_id
  AND c.owner_tenant_id IS NULL
  AND c.company_type IN ('CLIENT', 'SUPPLIER');

UPDATE company_table
SET owner_tenant_id = (SELECT id FROM company_table WHERE is_master = true ORDER BY created_at LIMIT 1)
WHERE owner_tenant_id IS NULL
  AND company_type IN ('CLIENT', 'SUPPLIER');

-- 2. Motoristas: tenant do conjunto da escala mais recente; senão o tenant do usuário (se for tenant)
UPDATE driver_table d
SET company_id = sub.company_id
FROM (
    SELECT DISTINCT ON (w.driver_id) w.driver_id, g.company_id
    FROM work_schedule w
    JOIN equipament_group_table g ON g.id = w.equipament_group_id
    WHERE g.company_id IS NOT NULL
    ORDER BY w.driver_id, w.created_at DESC
) sub
WHERE d.id = sub.driver_id
  AND (d.company_id IS NULL OR d.company_id IN (
        SELECT id FROM company_table WHERE company_type IN ('CLIENT', 'SUPPLIER')));

UPDATE driver_table d
SET company_id = u.company_id
FROM user_table u
JOIN company_table c ON c.id = u.company_id
WHERE d.user_id = u.id
  AND d.company_id IS NULL
  AND c.company_type NOT IN ('CLIENT', 'SUPPLIER');

UPDATE driver_table
SET company_id = (SELECT id FROM company_table WHERE is_master = true ORDER BY created_at LIMIT 1)
WHERE company_id IS NULL;

-- 3. Usuários motoristas gravados em empresa-cliente passam para o tenant do cadastro de motorista
UPDATE user_table u
SET company_id = d.company_id
FROM driver_table d
WHERE d.user_id = u.id
  AND u.company_id IN (SELECT id FROM company_table WHERE company_type IN ('CLIENT', 'SUPPLIER'))
  AND d.company_id NOT IN (SELECT id FROM company_table WHERE company_type IN ('CLIENT', 'SUPPLIER'));

-- 4. Veículos gravados em empresa-cliente passam para o tenant dono daquele cliente
UPDATE equipament_table e
SET company_id = c.owner_tenant_id
FROM company_table c
WHERE e.company_id = c.id
  AND c.company_type IN ('CLIENT', 'SUPPLIER')
  AND c.owner_tenant_id IS NOT NULL;

-- 5. Conjuntos e escalas acompanham o dono da tração
UPDATE equipament_group_table g
SET company_id = e.company_id
FROM equipament_table e
WHERE e.id = g.equipament1_id
  AND (g.company_id IS NULL OR g.company_id <> e.company_id);

UPDATE equipament_group_table SET status = 'DISPONIVEL' WHERE status IS NULL;

UPDATE work_schedule w
SET company_id = g.company_id
FROM equipament_group_table g
WHERE g.id = w.equipament_group_id
  AND (w.company_id IS NULL OR w.company_id <> g.company_id);

-- 6. Endereços: dono = tenant dono da empresa / da remessa / o próprio tenant; senão o Master
UPDATE address_table a
SET company_id = COALESCE(c.owner_tenant_id, c.id)
FROM company_table c
WHERE c.address_id = a.id
  AND (a.company_id IS NULL OR a.company_id IN (
        SELECT id FROM company_table WHERE company_type IN ('CLIENT', 'SUPPLIER')));

UPDATE address_table a
SET company_id = s.company_id
FROM shipment_table s
WHERE s.shipment_address_id = a.id
  AND a.company_id IS NULL
  AND s.company_id IS NOT NULL;

UPDATE address_table
SET company_id = (SELECT id FROM company_table WHERE is_master = true ORDER BY created_at LIMIT 1)
WHERE company_id IS NULL;

-- 7. Remessas, ocorrências e transportes órfãos
UPDATE shipment_table
SET company_id = (SELECT id FROM company_table WHERE is_master = true ORDER BY created_at LIMIT 1)
WHERE company_id IS NULL;

UPDATE occurrence_table o
SET company_id = t.transporter_id
FROM transport_table t
WHERE t.id = o.transport_id AND o.company_id IS NULL;

UPDATE transport_table t
SET transporter_id = g.company_id
FROM equipament_group_table g
WHERE g.id = t.equipament_group_id AND t.transporter_id IS NULL;
