-- Migração Step 6: Vínculo de Tenant (company_id) para Address e Driver

-- 1. address_table
ALTER TABLE address_table ADD COLUMN IF NOT EXISTS company_id UUID REFERENCES company_table(id);

UPDATE address_table a
SET company_id = c.id
FROM company_table c
WHERE c.address_id = a.id AND a.company_id IS NULL;

UPDATE address_table a
SET company_id = s.company_id
FROM shipment_table s
WHERE s.shipment_address_id = a.id AND a.company_id IS NULL AND s.company_id IS NOT NULL;

UPDATE address_table
SET company_id = '7f564f96-d90f-42cc-beb2-e37cf63a324d'
WHERE company_id IS NULL;

-- 2. driver_table
ALTER TABLE driver_table ADD COLUMN IF NOT EXISTS company_id UUID REFERENCES company_table(id);

UPDATE driver_table d
SET company_id = u.company_id
FROM user_table u
WHERE d.user_id = u.id AND d.company_id IS NULL AND u.company_id IS NOT NULL;

UPDATE driver_table
SET company_id = '7f564f96-d90f-42cc-beb2-e37cf63a324d'
WHERE company_id IS NULL;
