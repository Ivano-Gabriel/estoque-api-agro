ALTER TABLE loja
    ADD COLUMN IF NOT EXISTS tema_lanchonete_ativo BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE pedido_lanchonete
    ADD COLUMN IF NOT EXISTS data_operacao DATE;

UPDATE pedido_lanchonete
SET data_operacao = CAST(criado_em AS DATE)
WHERE data_operacao IS NULL;

ALTER TABLE pedido_lanchonete
    ALTER COLUMN data_operacao SET NOT NULL;

ALTER TABLE pedido_lanchonete
    DROP CONSTRAINT IF EXISTS uk_pedido_loja_numero;

ALTER TABLE pedido_lanchonete
    ADD CONSTRAINT uk_pedido_loja_data_numero UNIQUE (loja_id, data_operacao, numero);

CREATE INDEX IF NOT EXISTS idx_pedido_loja_data_numero
    ON pedido_lanchonete (loja_id, data_operacao, numero);
