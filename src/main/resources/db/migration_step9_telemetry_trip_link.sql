-- =========================================================================
-- MIGRAÇÃO STEP 9: Vínculo da telemetria com a viagem (idempotente)
--
-- Cada leitura de telemetria pode referenciar o transporte (viagem) em execução e a remessa
-- atendida. Ambas as colunas são opcionais: leituras fora de viagem ficam sem vínculo.
-- O Hibernate também cria as colunas com ddl-auto=update; aqui ficam as FKs com nome estável e os índices.
-- =========================================================================

ALTER TABLE telemetry_table ADD COLUMN IF NOT EXISTS shipment_id UUID;
ALTER TABLE telemetry_table ADD COLUMN IF NOT EXISTS transport_id UUID;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_telemetry_shipment') THEN
        ALTER TABLE telemetry_table
            ADD CONSTRAINT fk_telemetry_shipment FOREIGN KEY (shipment_id) REFERENCES shipment_table(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_telemetry_transport') THEN
        ALTER TABLE telemetry_table
            ADD CONSTRAINT fk_telemetry_transport FOREIGN KEY (transport_id) REFERENCES transport_table(id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_telemetry_transport_date ON telemetry_table (transport_id, date_time);
CREATE INDEX IF NOT EXISTS idx_telemetry_shipment_date ON telemetry_table (shipment_id, date_time);
