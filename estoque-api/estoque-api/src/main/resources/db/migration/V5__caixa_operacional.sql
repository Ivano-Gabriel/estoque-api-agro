ALTER TABLE loja ADD COLUMN IF NOT EXISTS caixa_operacional_ativo BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE loja ADD COLUMN IF NOT EXISTS lanchonete_ativa BOOLEAN NOT NULL DEFAULT FALSE;
CREATE TABLE IF NOT EXISTS caixa_sessao (
 id UUID PRIMARY KEY, loja_id BIGINT NOT NULL REFERENCES loja(id), operador_id BIGINT NOT NULL REFERENCES usuarios(id),
 status VARCHAR(20) NOT NULL, aberta_em TIMESTAMP NOT NULL, fechada_em TIMESTAMP,
 saldo_inicial NUMERIC(19,2) NOT NULL, total_vendas NUMERIC(19,2) NOT NULL DEFAULT 0,
 total_dinheiro NUMERIC(19,2) NOT NULL DEFAULT 0,
 total_suprimentos NUMERIC(19,2) NOT NULL DEFAULT 0, total_sangrias NUMERIC(19,2) NOT NULL DEFAULT 0,
 total_estornos NUMERIC(19,2) NOT NULL DEFAULT 0, saldo_esperado NUMERIC(19,2),
 total_estornos_dinheiro NUMERIC(19,2) NOT NULL DEFAULT 0,
 saldo_informado NUMERIC(19,2), diferenca NUMERIC(19,2), observacoes VARCHAR(500), versao BIGINT NOT NULL DEFAULT 0
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_caixa_aberto_operador ON caixa_sessao(loja_id,operador_id) WHERE status='ABERTO';
CREATE INDEX IF NOT EXISTS idx_caixa_sessao_loja_abertura ON caixa_sessao(loja_id,aberta_em);
CREATE INDEX IF NOT EXISTS idx_caixa_sessao_operador_status ON caixa_sessao(operador_id,status);
CREATE TABLE IF NOT EXISTS movimento_caixa (
 id UUID PRIMARY KEY, sessao_id UUID NOT NULL REFERENCES caixa_sessao(id), usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
 venda_id UUID REFERENCES venda(id), tipo VARCHAR(20) NOT NULL, valor NUMERIC(19,2) NOT NULL,
 descricao VARCHAR(300), criado_em TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_movimento_caixa_sessao_data ON movimento_caixa(sessao_id,criado_em);
