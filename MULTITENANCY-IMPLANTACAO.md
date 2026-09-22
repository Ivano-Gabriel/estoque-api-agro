# Implantação multi-tenant

## O que muda

- Um único backend e banco atendem várias lojas.
- Usuários, produtos, categorias, movimentações, caixa e operações possuem loja.
- A loja é obtida da sessão; o cliente não envia `loja_id`.
- O financeiro pode ser ligado ou desligado por loja.
- Produtos aceitam descrição e URL de foto do Cloudinary.
- O perfil `SUPER_ADMIN` cria, configura, bloqueia e reativa lojas.

## Variáveis novas no Render

```text
PLATFORM_ADMIN_EMAIL=
PLATFORM_ADMIN_PASSWORD=
```

Use credenciais exclusivas da plataforma, diferentes das contas das clientes.

## Variáveis novas no Vercel

```text
VITE_CLOUDINARY_CLOUD_NAME=
VITE_CLOUDINARY_UPLOAD_PRESET=
```

O preset deve ser unsigned, aceitar somente imagens JPG/PNG/WebP, limitar o tamanho
a 5 MB e aplicar as restrições de transformação disponíveis no Cloudinary.

## Ordem segura

1. Criar uma branch de backup no Neon.
2. Rodar `mvnw.cmd clean test` no backend.
3. Rodar `npm run build`, `npm run lint` e `npm test` no frontend.
4. Publicar primeiro o backend e conferir `/ping` e o login da loja atual.
5. Configurar o `SUPER_ADMIN` no Render e reiniciar o serviço.
6. Publicar o frontend e configurar as variáveis do Cloudinary no Vercel.
7. Entrar como `SUPER_ADMIN`, renomear a loja piloto e cadastrar a segunda loja.
8. Confirmar que cada loja enxerga somente seus próprios produtos.

## Migração automática

Na primeira inicialização, o sistema cria `Loja piloto` e vincula a ela os dados
single-tenant existentes. A migração não apaga produtos, usuários ou históricos.

Depois do deploy, confira no Neon:

```sql
SELECT id, nome, slug, ativa, financeiro_ativo FROM loja ORDER BY id;
SELECT loja_id, COUNT(*) FROM produto GROUP BY loja_id ORDER BY loja_id;
SELECT loja_id, COUNT(*) FROM usuarios GROUP BY loja_id ORDER BY loja_id;
```

Não execute limpeza de produção antes de validar esses resultados.
