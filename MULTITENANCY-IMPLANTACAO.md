# Implantação multi-tenant

## O que muda

- Um único backend e banco atendem várias lojas.
- Usuários, produtos, categorias, movimentações, caixa e operações possuem loja.
- A loja é obtida da sessão; o cliente não envia `loja_id`.
- O financeiro pode ser ligado ou desligado por loja.
- A descrição existe para todas as lojas; fotos podem ser ligadas ou desligadas por loja.
- Uploads do Cloudinary são assinados pela API e separados em pastas por loja.
- O perfil `SUPER_ADMIN` cria, configura, bloqueia e reativa lojas.

## Variáveis novas no Render

```text
PLATFORM_ADMIN_EMAIL=
PLATFORM_ADMIN_PASSWORD=
CLOUDINARY_CLOUD_NAME=
CLOUDINARY_API_KEY=
CLOUDINARY_API_SECRET=
CLOUDINARY_UPLOAD_PRESET=estoque_produtos
```

Use credenciais exclusivas da plataforma, diferentes das contas das clientes. Nunca
coloque `CLOUDINARY_API_SECRET` no Vercel ou no código do frontend.

## Preset do Cloudinary

Crie o preset `estoque_produtos` como **signed**, aceite somente JPG/PNG/WebP,
limite o arquivo a 5 MB e aplique uma transformação de entrada que limite a imagem
a 1600 x 1600 pixels. A autorização temporária só é entregue a usuários autenticados
de lojas com o módulo de fotos ativo.

## Ordem segura

1. Criar uma branch de backup no Neon.
2. Rodar `mvnw.cmd clean test` no backend.
3. Rodar `npm run build`, `npm run lint` e `npm test` no frontend.
4. Publicar primeiro o backend e conferir `/ping` e o login da loja atual.
5. Configurar o `SUPER_ADMIN` no Render e reiniciar o serviço.
6. Publicar o frontend; ele não recebe nem armazena o segredo do Cloudinary.
7. Entrar como `SUPER_ADMIN`, renomear a loja piloto e cadastrar a segunda loja.
8. Confirmar que cada loja enxerga somente seus próprios produtos.

## Migração automática

Na primeira inicialização, o sistema cria `Loja piloto` e vincula a ela os dados
single-tenant existentes. A migração não apaga produtos, usuários ou históricos.

Depois do deploy, confira no Neon:

```sql
SELECT id, nome, slug, ativa, financeiro_ativo, fotos_ativas FROM loja ORDER BY id;
SELECT loja_id, COUNT(*) FROM produto GROUP BY loja_id ORDER BY loja_id;
SELECT loja_id, COUNT(*) FROM usuarios GROUP BY loja_id ORDER BY loja_id;
```

Não execute limpeza de produção antes de validar esses resultados.
