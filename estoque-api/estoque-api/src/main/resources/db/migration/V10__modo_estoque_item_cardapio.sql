ALTER TABLE item_cardapio
    ADD COLUMN IF NOT EXISTS baixa_produto_final BOOLEAN NOT NULL DEFAULT FALSE;

-- Recupera automaticamente produtos prontos que já possuíam unidades cadastradas
-- e não têm ficha técnica. Itens preparados continuam baixando seus ingredientes.
UPDATE item_cardapio ic
SET baixa_produto_final = TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM ficha_tecnica_item f WHERE f.item_cardapio_id = ic.id
)
AND EXISTS (
    SELECT 1 FROM produto p
    WHERE p.id = ic.produto_id AND p.quantidade_estoque > 0
);

UPDATE produto p
SET controla_estoque = TRUE
WHERE EXISTS (
    SELECT 1 FROM item_cardapio ic
    WHERE ic.produto_id = p.id AND ic.baixa_produto_final = TRUE
);
