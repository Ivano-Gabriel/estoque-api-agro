# Correções para o piloto single-tenant

Este pacote deve ser aplicado junto com o frontend `codex/pilot-hardening`.
Não há alterações automáticas no Render, Neon ou Vercel ao aplicar o patch localmente.

## O que muda

- Venda/reposição exigem `Idempotency-Key` UUID e retornam 204. Repetir a mesma chave e os mesmos dados não repete a operação. Trocar os dados ou o usuário da chave é rejeitado.
- O registro da chave, estoque, transação e caixa são confirmados juntos ou revertidos juntos. Uma trava de banco no caixa serializa as alterações nesta loja; não é arquitetura multitenant.
- O caixa passa a ter ID explícito 1. Se houver registros com outros IDs, a inicialização para: NÃO apagar ou juntar saldos automaticamente.
- Produtos inativos somem do catálogo, mas continuam acessíveis às transações antigas. Restaurar não cria movimento financeiro.
- Nome + categoria duplicados, incluindo lixeira, são rejeitados em cadastro/edição/importação. Cadastros antigos duplicados não são apagados automaticamente: revisar manualmente.
- Atualização sem validade conserva a data anterior; uma data preenchida a substitui. Remover uma validade intencionalmente não faz parte desta alteração.
- Gráfico mensal artificial removido. Estoque a preço de venda e lucro bruto passam a ter nomes explícitos. Lucro bruto não desconta despesas operacionais. Estoque inicial não lança pagamento no caixa.
- Login: 10 tentativas por e-mail em 5 minutos e 60 totais/minuto por instância, inclusive sucessos. Reiniciar zera o limite. Não substitui proteção de borda contra ataques distribuídos.
- Administrador pode bloquear/liberar funcionárias, encerrar sessões e redefinir senha. Não exclui usuário nem transações.
- Versão de sessão no JWT: tokens anteriores ao pacote deixam de funcionar; todos devem entrar novamente. Troca de senha/revogação/bloqueio invalida tokens anteriores nas próximas requisições. Operações já em execução podem concluir.
- A senha do administrador continua sincronizada com `ADMIN_PASSWORD` na inicialização. Alterá-la somente no banco será desfeito ao reiniciar. Não há senha padrão.
- Reset e exclusão permanente foram preservados. Agora exigem AMBOS `ferramentas-teste` e `app.test-tools.enabled=true`. Nunca habilitar em produção.

## Antes de publicar

1. Escolher uma janela sem movimentações. Confirmar que os dados atuais são testes ou dados reais; nunca limpar automaticamente.
2. Fazer backup lógico do PostgreSQL e restaurar em banco descartável. Com as variáveis padrão `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD`, `PGSSLMODE=require` configuradas de forma privada, usar `pg_dump --format=custom --file=estoque-antes-piloto.dump`. Não colocar senha na linha do comando nem no Git.
3. Usar outro banco vazio para verificar `pg_restore --no-owner --no-acl --dbname=<banco-descartavel> estoque-antes-piloto.dump`; conferir tabelas, usuários e totais. Não restaurar por cima do banco da loja. Guardar o backup fora do repositório, com acesso restrito.
4. Consultar `SELECT id, total_entradas, total_saidas, saldo_liquido FROM fluxo_caixa;`. Deve existir só ID 1 ou nenhum registro. Se existirem outros IDs, conciliar antes do deploy.
5. Este projeto ainda usa `ddl-auto=update`. A atualização adiciona `operacao_estoque` e campos de controle em `usuarios`; verificar o esquema em cópia do banco antes. Este pacote não substitui um processo de migrações versionadas.
6. Conferir `JWT_SECRET`, conexão Neon, `CORS_ALLOWED_ORIGINS`, `WHATSAPP_REPORT_NUMBER`, `ADMIN_EMAIL` e `ADMIN_PASSWORD` sem compartilhar seus valores. Trocar qualquer segredo exposto. Verificar que nenhum perfil/filtro habilita ferramentas de teste.
7. Executar `./mvnw clean test` com Java 21; no Windows, `.\mvnw.cmd clean test`. Testes usam H2, não devem apontar para Neon. Remover variáveis `SPRING_DATASOURCE_*` de terminais de teste se estiverem sobrescrevendo a configuração de teste.
8. Publicar backend e frontend na mesma janela. A interface antiga não envia a chave e a API nova rejeita suas movimentações. Fechar abas antigas, reabrir o app/PWA e entrar novamente.

## Verificação após publicação

- Ping, login de admin e funcionária, bloqueio 403 do financeiro para funcionária.
- Em produto de teste: vender 2 a R$150 com custo unitário R$100 => receita R$300 e lucro bruto R$100; clique repetido não duplica.
- Queda de conexão: se houver operação pendente, usar “Confirmar operação pendente” no MESMO navegador. O envio conserva a chave. Não lançar outra venda para compensar sem conferir histórico.
- Editar o nome de produto com validade, confirmar data preservada.
- Produto com venda → lixeira → histórico e relatório → restaurar. Totais devem permanecer.
- Importar produto da lixeira: rejeitar duplicidade. Restaurar o produto existente.
- Bloquear funcionária: sessão anterior recebe 401. Desbloquear e entrar novamente. Trocar senha e confirmar a rejeição da antiga.
- Testar PWA no equipamento da loja, atualização e reconexão. NÃO há registro de vendas offline.
- Configurar número da chefe e conferir envio manual diário/semanal/mensal no WhatsApp. Funcionárias veem esses totais por decisão de negócio já aprovada.

## Operação inicial e recuperação

Conferir estoque e caixa ao fim de cada expediente do piloto. Manter processo paralelo na primeira validação.
Definir responsável pelo suporte e testar backup/restauração periodicamente. O plano da hospedagem e a retenção real de backups precisam ser confirmados no painel.
Se algo falhar, parar novas movimentações e conservar dados/logs sem segredos. Reverter frontend e backend juntos para os commits anteriores somente após avaliar as alterações do banco; nunca restaurar backup apagando vendas posteriores sem conciliação.
