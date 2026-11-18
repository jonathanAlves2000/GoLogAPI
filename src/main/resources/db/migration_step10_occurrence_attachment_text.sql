-- =========================================================================
-- MIGRAÇÃO STEP 10: Anexo da ocorrência como TEXT (idempotente)
--
-- O app envia a assinatura do recebedor como SVG cru no campo attachment,
-- que não cabe em varchar(255). O ddl-auto=update do Hibernate não altera o
-- tipo de colunas existentes, por isso a conversão fica aqui.
-- =========================================================================

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'occurrence_table'
          AND column_name = 'attachment'
          AND data_type <> 'text'
    ) THEN
        ALTER TABLE occurrence_table ALTER COLUMN attachment TYPE TEXT;
    END IF;
END $$;
