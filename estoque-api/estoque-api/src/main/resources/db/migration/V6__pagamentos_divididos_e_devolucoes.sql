CREATE TABLE IF NOT EXISTS pagamento_venda (
 id BIGSERIAL PRIMARY KEY,
 venda_id UUID NOT NULL REFERENCES venda(id) ON DELETE CASCADE,
 forma VARCHAR(30) NOT NULL,
 valor NUMERIC(19,2) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_pagamento_venda_venda ON pagamento_venda(venda_id);

INSERT INTO pagamento_venda(venda_id, forma, valor)
SELECT v.id, v.forma_pagamento, v.total
FROM venda v
WHERE v.total > 0
  AND NOT EXISTS (SELECT 1 FROM pagamento_venda p WHERE p.venda_id=v.id);

ALTER TABLE venda_item ADD COLUMN IF NOT EXISTS quantidade_devolvida INTEGER NOT NULL DEFAULT 0;
