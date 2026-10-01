CREATE TABLE IF NOT EXISTS limite_login (
    id VARCHAR(80) PRIMARY KEY,
    inicio TIMESTAMPTZ NOT NULL,
    tentativas INTEGER NOT NULL DEFAULT 0,
    versao BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_limite_login_inicio ON limite_login(inicio);
INSERT INTO limite_login(id, inicio, tentativas, versao)
VALUES ('global', NOW(), 0, 0)
ON CONFLICT (id) DO NOTHING;
