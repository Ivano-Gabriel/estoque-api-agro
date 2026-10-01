ALTER TABLE venda ADD COLUMN IF NOT EXISTS total_devolvido NUMERIC(19,2) NOT NULL DEFAULT 0;
ALTER TABLE venda ALTER COLUMN status TYPE VARCHAR(30);
ALTER TABLE venda_item ADD COLUMN IF NOT EXISTS valor_devolvido NUMERIC(19,2) NOT NULL DEFAULT 0;
CREATE TABLE IF NOT EXISTS devolucao_venda (
 id UUID PRIMARY KEY, loja_id BIGINT NOT NULL REFERENCES loja(id), venda_id UUID NOT NULL REFERENCES venda(id),
 usuario_id BIGINT NOT NULL REFERENCES usuarios(id), forma_reembolso VARCHAR(30) NOT NULL,
 valor NUMERIC(19,2) NOT NULL, motivo VARCHAR(300) NOT NULL, criada_em TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_devolucao_loja_data ON devolucao_venda(loja_id,criada_em);
CREATE TABLE IF NOT EXISTS devolucao_venda_item (
 id BIGSERIAL PRIMARY KEY, devolucao_id UUID NOT NULL REFERENCES devolucao_venda(id) ON DELETE CASCADE,
 venda_item_id BIGINT NOT NULL REFERENCES venda_item(id), quantidade INTEGER NOT NULL, valor NUMERIC(19,2) NOT NULL
);
