ALTER TABLE produto ADD COLUMN IF NOT EXISTS sku VARCHAR(60);
ALTER TABLE produto ADD COLUMN IF NOT EXISTS codigo_barras VARCHAR(50);
ALTER TABLE produto ADD COLUMN IF NOT EXISTS variacao VARCHAR(120);
ALTER TABLE produto ADD COLUMN IF NOT EXISTS estoque_minimo INTEGER NOT NULL DEFAULT 5;

CREATE UNIQUE INDEX IF NOT EXISTS uk_produto_loja_sku
    ON produto(loja_id, LOWER(sku)) WHERE sku IS NOT NULL AND TRIM(sku) <> '';
CREATE UNIQUE INDEX IF NOT EXISTS uk_produto_loja_codigo_barras
    ON produto(loja_id, LOWER(codigo_barras))
    WHERE codigo_barras IS NOT NULL AND TRIM(codigo_barras) <> '';
CREATE INDEX IF NOT EXISTS idx_produto_loja_variacao
    ON produto(loja_id, LOWER(nome), LOWER(COALESCE(variacao, '')));
