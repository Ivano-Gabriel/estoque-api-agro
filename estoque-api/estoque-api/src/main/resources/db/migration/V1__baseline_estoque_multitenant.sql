-- Baseline reproduzível do schema atual.
-- Todos os comandos são seguros para o banco piloto que já foi criado pelo Hibernate.

CREATE TABLE IF NOT EXISTS loja (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    slug VARCHAR(60) NOT NULL,
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    financeiro_ativo BOOLEAN NOT NULL DEFAULT TRUE,
    fotos_ativas BOOLEAN NOT NULL DEFAULT FALSE,
    notas_fiscais_ativas BOOLEAN NOT NULL DEFAULT FALSE,
    whatsapp VARCHAR(20),
    versao BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_loja_slug UNIQUE (slug)
);

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    loja_id BIGINT REFERENCES loja(id),
    senha VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'FUNCIONARIA',
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    versao_sessao INTEGER NOT NULL DEFAULT 0,
    versao BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS categoria (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255),
    loja_id BIGINT REFERENCES loja(id)
);

CREATE TABLE IF NOT EXISTS produto (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255),
    tipo VARCHAR(255),
    descricao VARCHAR(500),
    imagem_url VARCHAR(500),
    loja_id BIGINT REFERENCES loja(id),
    preco NUMERIC(19,2) NOT NULL DEFAULT 0,
    custo_medio NUMERIC(19,2) NOT NULL DEFAULT 0,
    data_validade DATE,
    categoria_id BIGINT NOT NULL REFERENCES categoria(id),
    versao BIGINT NOT NULL DEFAULT 0,
    quantidade_estoque INTEGER NOT NULL DEFAULT 0,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS fluxo_caixa (
    id BIGINT PRIMARY KEY,
    loja_id BIGINT UNIQUE REFERENCES loja(id),
    versao BIGINT NOT NULL DEFAULT 0,
    total_entradas NUMERIC(19,2) NOT NULL DEFAULT 0,
    total_saidas NUMERIC(19,2) NOT NULL DEFAULT 0,
    saldo_liquido NUMERIC(19,2) NOT NULL DEFAULT 0,
    ultima_atualizacao TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cliente (
    id BIGSERIAL PRIMARY KEY,
    loja_id BIGINT NOT NULL REFERENCES loja(id),
    nome VARCHAR(120) NOT NULL,
    telefone VARCHAR(15),
    email VARCHAR(150),
    observacoes VARCHAR(500),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS cliente_produto_favorito (
    cliente_id BIGINT NOT NULL REFERENCES cliente(id),
    produto_id BIGINT NOT NULL REFERENCES produto(id),
    CONSTRAINT uk_cliente_produto_favorito UNIQUE (cliente_id, produto_id)
);

CREATE TABLE IF NOT EXISTS venda (
    id UUID PRIMARY KEY,
    loja_id BIGINT NOT NULL REFERENCES loja(id),
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    cliente_id BIGINT REFERENCES cliente(id),
    assinatura VARCHAR(64) NOT NULL,
    forma_pagamento VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    subtotal NUMERIC(19,2) NOT NULL,
    desconto NUMERIC(19,2) NOT NULL,
    total NUMERIC(19,2) NOT NULL,
    valor_recebido NUMERIC(19,2),
    troco NUMERIC(19,2),
    criada_em TIMESTAMP NOT NULL,
    cancelada_em TIMESTAMP,
    cancelada_por_id BIGINT REFERENCES usuarios(id),
    motivo_cancelamento VARCHAR(300),
    versao BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS venda_item (
    id BIGSERIAL PRIMARY KEY,
    venda_id UUID NOT NULL REFERENCES venda(id),
    produto_id BIGINT NOT NULL REFERENCES produto(id),
    nome_produto VARCHAR(120) NOT NULL,
    tipo_produto VARCHAR(40) NOT NULL,
    quantidade INTEGER NOT NULL,
    preco_unitario NUMERIC(19,2) NOT NULL,
    custo_unitario NUMERIC(19,2) NOT NULL,
    subtotal NUMERIC(19,2) NOT NULL,
    desconto_rateado NUMERIC(19,2) NOT NULL,
    total NUMERIC(19,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS transacao (
    id BIGSERIAL PRIMARY KEY,
    loja_id BIGINT REFERENCES loja(id),
    produto_id BIGINT REFERENCES produto(id),
    usuario_id BIGINT REFERENCES usuarios(id),
    cliente_id BIGINT REFERENCES cliente(id),
    venda_id UUID REFERENCES venda(id),
    forma_pagamento VARCHAR(30),
    estornada BOOLEAN NOT NULL DEFAULT FALSE,
    tipo VARCHAR(255) NOT NULL,
    quantidade INTEGER NOT NULL,
    preco_unitario NUMERIC(19,2) NOT NULL,
    valor_total NUMERIC(19,2) NOT NULL,
    custo_unitario NUMERIC(19,2) NOT NULL DEFAULT 0,
    lucro NUMERIC(19,2) NOT NULL DEFAULT 0,
    data TIMESTAMP NOT NULL,
    descricao VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS operacao_estoque (
    id UUID PRIMARY KEY,
    loja_id BIGINT REFERENCES loja(id),
    assinatura VARCHAR(200) NOT NULL,
    transacao_id BIGINT UNIQUE REFERENCES transacao(id)
);

CREATE TABLE IF NOT EXISTS nota_recebida (
    id BIGSERIAL PRIMARY KEY,
    loja_id BIGINT NOT NULL REFERENCES loja(id),
    fornecedor VARCHAR(120) NOT NULL,
    documento_fornecedor VARCHAR(14),
    numero VARCHAR(30) NOT NULL,
    serie VARCHAR(10),
    chave_acesso VARCHAR(44),
    data_emissao DATE,
    data_recebimento DATE NOT NULL,
    valor_total NUMERIC(19,2) NOT NULL,
    conferida BOOLEAN NOT NULL,
    estoque_atualizado BOOLEAN NOT NULL,
    observacoes VARCHAR(1000),
    criada_em TIMESTAMP NOT NULL,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    CONSTRAINT uk_nota_loja_chave UNIQUE (loja_id, chave_acesso)
);

CREATE TABLE IF NOT EXISTS nota_recebida_item (
    id BIGSERIAL PRIMARY KEY,
    nota_id BIGINT NOT NULL REFERENCES nota_recebida(id),
    produto_id BIGINT NOT NULL REFERENCES produto(id),
    quantidade INTEGER NOT NULL,
    custo_unitario NUMERIC(19,2) NOT NULL
);

-- Compatibilidade com bancos criados por versões intermediárias do piloto.
ALTER TABLE loja ADD COLUMN IF NOT EXISTS notas_fiscais_ativas BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE produto ADD COLUMN IF NOT EXISTS descricao VARCHAR(500);
ALTER TABLE produto ADD COLUMN IF NOT EXISTS imagem_url VARCHAR(500);
ALTER TABLE produto ADD COLUMN IF NOT EXISTS custo_medio NUMERIC(19,2) NOT NULL DEFAULT 0;
ALTER TABLE produto ADD COLUMN IF NOT EXISTS versao BIGINT NOT NULL DEFAULT 0;
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS ativo BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS versao_sessao INTEGER NOT NULL DEFAULT 0;
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS versao BIGINT NOT NULL DEFAULT 0;
ALTER TABLE transacao ADD COLUMN IF NOT EXISTS forma_pagamento VARCHAR(30);
ALTER TABLE transacao ADD COLUMN IF NOT EXISTS estornada BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_produto_loja_ativo ON produto(loja_id, ativo);
CREATE INDEX IF NOT EXISTS idx_produto_loja_nome ON produto(loja_id, nome);
CREATE INDEX IF NOT EXISTS idx_categoria_loja_nome ON categoria(loja_id, nome);
CREATE INDEX IF NOT EXISTS idx_transacao_loja_data ON transacao(loja_id, data);
CREATE INDEX IF NOT EXISTS idx_transacao_venda ON transacao(venda_id);
CREATE INDEX IF NOT EXISTS idx_usuario_loja_role ON usuarios(loja_id, role);
CREATE INDEX IF NOT EXISTS idx_cliente_loja_nome ON cliente(loja_id, nome);
CREATE INDEX IF NOT EXISTS idx_venda_loja_data ON venda(loja_id, criada_em);
CREATE INDEX IF NOT EXISTS idx_venda_loja_status ON venda(loja_id, status);
CREATE INDEX IF NOT EXISTS idx_nota_recebida_loja_data ON nota_recebida(loja_id, data_recebimento);

DO $$
DECLARE constraint_name TEXT;
BEGIN
    FOR constraint_name IN
        SELECT conname FROM pg_constraint
        WHERE conrelid = 'usuarios'::regclass AND contype = 'c'
          AND pg_get_constraintdef(oid) ILIKE '%role%'
    LOOP
        EXECUTE format('ALTER TABLE usuarios DROP CONSTRAINT %I', constraint_name);
    END LOOP;
    ALTER TABLE usuarios ADD CONSTRAINT usuarios_role_check
        CHECK (role IN ('SUPER_ADMIN', 'ADMIN', 'FUNCIONARIA', 'OPERADOR'));
END $$;
