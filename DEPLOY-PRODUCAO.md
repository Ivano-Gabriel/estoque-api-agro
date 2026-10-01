# Implantação segura

## Antes do primeiro deploy desta versão

1. Crie um branch/backup do banco Neon a partir de `production`.
2. Confirme que o Render aponta para o banco correto.
3. Mantenha `JPA_DDL_AUTO=validate` e `FLYWAY_ENABLED=true`.
4. Configure `ADMIN_STORE_SLUG` se ainda usar `ADMIN_EMAIL` e `ADMIN_PASSWORD`. O sistema não escolhe mais a primeira loja.
5. Configure o segundo fator do superadministrador antes de reiniciar.

## Segundo fator do superadministrador

Gere um segredo Base32 de 32 caracteres em um gerenciador TOTP confiável. Cadastre a mesma chave:

- no Render, como `PLATFORM_ADMIN_MFA_SECRET`;
- no Google Authenticator, Microsoft Authenticator, 1Password ou equivalente, como conta “Estoque Superadmin”.

Use no autenticador:

- algoritmo: `SHA1`;
- dígitos: `6`;
- período: `30 segundos`.

O backend se recusa a iniciar com `PLATFORM_ADMIN_EMAIL`/`PASSWORD` sem MFA. Isso evita publicar uma conta global desprotegida.

## Ordem de publicação

1. Faça backup/branch do Neon.
2. Cadastre todas as variáveis novas no Render.
3. Publique o backend e aguarde `/ping` responder `pong`.
4. Verifique no log que as migrações `V1` a `V7` foram aplicadas.
5. Publique o frontend.
6. Teste login comum, login superadmin com TOTP, duas lojas diferentes, venda, pagamento dividido, devolução e fechamento de caixa.

## Proteção do GitHub

Em cada repositório, abra **Settings → Branches → Add branch protection rule** para `main` e marque:

- Require a pull request before merging;
- Require status checks to pass;
- selecione o check `test` do workflow correspondente;
- Do not allow bypassing the above settings.

O workflow impede regressões somente depois que essa proteção externa estiver ativa.

## Retorno seguro

Se a migração falhar, não altere manualmente a tabela de histórico do Flyway. Preserve o log, volte o serviço para o commit anterior e conecte temporariamente ao branch de backup somente durante a investigação.

## Rotina mínima

- Antes de cada release: criar branch de backup no Neon.
- Semanalmente: testar restauração em um branch separado.
- Mensalmente: revisar usuários, auditoria, imagens órfãs e uso do Cloudinary.
- Antes de compartilhar logs: remover URLs, senhas, JWTs, chaves e dados de clientes.
