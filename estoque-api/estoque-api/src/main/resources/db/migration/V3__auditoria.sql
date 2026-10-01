CREATE TABLE IF NOT EXISTS auditoria (
    id BIGSERIAL PRIMARY KEY,
    loja_id BIGINT REFERENCES loja(id),
    usuario_id BIGINT REFERENCES usuarios(id),
    usuario_email VARCHAR(150) NOT NULL,
    acao VARCHAR(60) NOT NULL,
    recurso VARCHAR(60) NOT NULL,
    recurso_id VARCHAR(80),
    detalhes VARCHAR(2000),
    criada_em TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_auditoria_loja_data ON auditoria(loja_id, criada_em);
CREATE INDEX IF NOT EXISTS idx_auditoria_recurso ON auditoria(loja_id, recurso, recurso_id);
