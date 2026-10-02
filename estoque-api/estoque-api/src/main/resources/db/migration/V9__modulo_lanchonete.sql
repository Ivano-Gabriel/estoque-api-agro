ALTER TABLE produto ADD COLUMN IF NOT EXISTS controla_estoque BOOLEAN NOT NULL DEFAULT TRUE;

CREATE TABLE IF NOT EXISTS item_cardapio (
 id BIGSERIAL PRIMARY KEY, loja_id BIGINT NOT NULL REFERENCES loja(id), produto_id BIGINT NOT NULL REFERENCES produto(id),
 nome_cozinha VARCHAR(80), estacao VARCHAR(40) NOT NULL DEFAULT 'COZINHA', tempo_preparo_minutos INTEGER NOT NULL DEFAULT 15,
 disponivel BOOLEAN NOT NULL DEFAULT TRUE, destaque BOOLEAN NOT NULL DEFAULT FALSE, ordem INTEGER NOT NULL DEFAULT 0,
 versao BIGINT NOT NULL DEFAULT 0, CONSTRAINT uk_item_cardapio_loja_produto UNIQUE(loja_id,produto_id)
);
CREATE INDEX IF NOT EXISTS idx_item_cardapio_loja_disponivel ON item_cardapio(loja_id,disponivel,ordem);

CREATE TABLE IF NOT EXISTS ficha_tecnica_item (
 id BIGSERIAL PRIMARY KEY, item_cardapio_id BIGINT NOT NULL REFERENCES item_cardapio(id) ON DELETE CASCADE,
 ingrediente_id BIGINT NOT NULL REFERENCES produto(id), quantidade INTEGER NOT NULL,
 CONSTRAINT uk_ficha_item_ingrediente UNIQUE(item_cardapio_id,ingrediente_id), CONSTRAINT ck_ficha_quantidade CHECK(quantidade>0)
);

CREATE TABLE IF NOT EXISTS grupo_adicional (
 id BIGSERIAL PRIMARY KEY, loja_id BIGINT NOT NULL REFERENCES loja(id), nome VARCHAR(80) NOT NULL,
 minimo INTEGER NOT NULL DEFAULT 0, maximo INTEGER NOT NULL DEFAULT 1, obrigatorio BOOLEAN NOT NULL DEFAULT FALSE,
 ativo BOOLEAN NOT NULL DEFAULT TRUE, ordem INTEGER NOT NULL DEFAULT 0, versao BIGINT NOT NULL DEFAULT 0,
 CONSTRAINT ck_grupo_limites CHECK(minimo>=0 AND maximo>=1 AND maximo>=minimo)
);
CREATE TABLE IF NOT EXISTS opcao_adicional (
 id BIGSERIAL PRIMARY KEY, grupo_id BIGINT NOT NULL REFERENCES grupo_adicional(id) ON DELETE CASCADE,
 produto_id BIGINT NOT NULL REFERENCES produto(id), ingrediente_id BIGINT REFERENCES produto(id),
 quantidade_insumo INTEGER NOT NULL DEFAULT 0, ativo BOOLEAN NOT NULL DEFAULT TRUE, ordem INTEGER NOT NULL DEFAULT 0,
 CONSTRAINT uk_opcao_grupo_produto UNIQUE(grupo_id,produto_id), CONSTRAINT ck_opcao_insumo CHECK(quantidade_insumo>=0)
);
CREATE TABLE IF NOT EXISTS item_cardapio_grupo (
 item_cardapio_id BIGINT NOT NULL REFERENCES item_cardapio(id) ON DELETE CASCADE,
 grupo_id BIGINT NOT NULL REFERENCES grupo_adicional(id) ON DELETE CASCADE,
 PRIMARY KEY(item_cardapio_id,grupo_id)
);

CREATE TABLE IF NOT EXISTS mesa_lanchonete (
 id BIGSERIAL PRIMARY KEY, loja_id BIGINT NOT NULL REFERENCES loja(id), nome VARCHAR(40) NOT NULL,
 lugares INTEGER NOT NULL DEFAULT 4, ativa BOOLEAN NOT NULL DEFAULT TRUE, ordem INTEGER NOT NULL DEFAULT 0,
 versao BIGINT NOT NULL DEFAULT 0, CONSTRAINT uk_mesa_loja_nome UNIQUE(loja_id,nome), CONSTRAINT ck_mesa_lugares CHECK(lugares>0)
);

CREATE TABLE IF NOT EXISTS pedido_lanchonete (
 id UUID PRIMARY KEY, loja_id BIGINT NOT NULL REFERENCES loja(id), usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
 cliente_id BIGINT REFERENCES cliente(id), mesa_id BIGINT REFERENCES mesa_lanchonete(id), venda_id UUID REFERENCES venda(id),
 assinatura VARCHAR(64) NOT NULL, numero BIGINT NOT NULL, tipo VARCHAR(20) NOT NULL, status VARCHAR(30) NOT NULL,
 identificacao VARCHAR(80), telefone VARCHAR(20), endereco VARCHAR(300), observacoes VARCHAR(500),
 subtotal NUMERIC(19,2) NOT NULL, desconto NUMERIC(19,2) NOT NULL DEFAULT 0, total NUMERIC(19,2) NOT NULL,
 criado_em TIMESTAMP NOT NULL, atualizado_em TIMESTAMP NOT NULL, enviado_em TIMESTAMP, pronto_em TIMESTAMP,
 finalizado_em TIMESTAMP, cancelado_em TIMESTAMP, motivo_cancelamento VARCHAR(300), versao BIGINT NOT NULL DEFAULT 0,
 CONSTRAINT uk_pedido_loja_numero UNIQUE(loja_id,numero)
);
CREATE INDEX IF NOT EXISTS idx_pedido_loja_status_data ON pedido_lanchonete(loja_id,status,criado_em);
CREATE INDEX IF NOT EXISTS idx_pedido_mesa_status ON pedido_lanchonete(mesa_id,status);

CREATE TABLE IF NOT EXISTS pedido_lanchonete_item (
 id BIGSERIAL PRIMARY KEY, pedido_id UUID NOT NULL REFERENCES pedido_lanchonete(id) ON DELETE CASCADE,
 item_cardapio_id BIGINT NOT NULL REFERENCES item_cardapio(id), produto_id BIGINT NOT NULL REFERENCES produto(id),
 nome VARCHAR(120) NOT NULL, quantidade INTEGER NOT NULL, preco_unitario NUMERIC(19,2) NOT NULL,
 subtotal NUMERIC(19,2) NOT NULL, observacoes VARCHAR(300), estacao VARCHAR(40) NOT NULL,
 CONSTRAINT ck_pedido_item_quantidade CHECK(quantidade>0)
);
CREATE TABLE IF NOT EXISTS pedido_lanchonete_adicional (
 id BIGSERIAL PRIMARY KEY, pedido_item_id BIGINT NOT NULL REFERENCES pedido_lanchonete_item(id) ON DELETE CASCADE,
 opcao_id BIGINT NOT NULL REFERENCES opcao_adicional(id), produto_id BIGINT NOT NULL REFERENCES produto(id),
 nome VARCHAR(120) NOT NULL, quantidade INTEGER NOT NULL DEFAULT 1, preco_unitario NUMERIC(19,2) NOT NULL
);
CREATE TABLE IF NOT EXISTS pedido_lanchonete_consumo (
 id BIGSERIAL PRIMARY KEY, pedido_id UUID NOT NULL REFERENCES pedido_lanchonete(id) ON DELETE CASCADE,
 produto_id BIGINT NOT NULL REFERENCES produto(id), quantidade INTEGER NOT NULL,
 custo_unitario NUMERIC(19,2) NOT NULL DEFAULT 0, restaurado BOOLEAN NOT NULL DEFAULT FALSE,
 CONSTRAINT uk_pedido_consumo_produto UNIQUE(pedido_id,produto_id), CONSTRAINT ck_consumo_quantidade CHECK(quantidade>0)
);
