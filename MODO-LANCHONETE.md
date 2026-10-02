# Modo lanchonete

O modo lanchonete transforma uma loja do sistema em uma operação de balcão, salão,
retirada e entrega sem retirar as funções normais de estoque. O módulo é isolado por
loja e só aparece quando um superadministrador o ativa.

## O que está incluído

- cardápio com foto, preço, destaque, estação e tempo estimado;
- ficha técnica que baixa automaticamente os ingredientes;
- grupos obrigatórios ou opcionais, como tamanho, ponto e adicionais;
- balcão, mesas, retirada e entrega;
- identificação, telefone, endereço, cliente cadastrado e observações;
- painel de cozinha atualizado automaticamente;
- comandas de 80 mm para cozinha;
- fluxo Recebido → Em preparo → Pronto → Entrega/Finalizado;
- recebimento em PIX, dinheiro, débito, crédito, outro ou pagamento dividido;
- integração com abertura e fechamento de caixa;
- histórico dos últimos 100 pedidos;
- cancelamento com estorno da venda e devolução dos insumos ao estoque;
- proteção contra duplo clique, resposta perdida e pedido repetido;
- auditoria e isolamento multiempresa em todas as operações.

## Ativação

1. Entre como superadministrador.
2. Na loja desejada, clique em **Ligar lanchonete**.
3. A administradora da loja precisa sair e entrar novamente para atualizar a sessão.
4. Abra **Lanchonete → Cardápio**.
5. Cadastre primeiro os produtos do estoque e do cardápio em **Gerenciar**.
6. Configure os adicionais, depois os itens do cardápio e por último as mesas.

## Modelo de cadastro

Cada ingrediente deve existir como produto controlado pelo estoque. Use sempre a menor
unidade prática: grama, mililitro, unidade ou porção. Se o estoque de carne é controlado
em gramas, um hambúrguer que usa 120 g deve consumir `120` na ficha técnica.

O item vendido também é um produto, mas ao entrar no cardápio passa a ser produzido sob
demanda. Por isso sua venda não baixa uma unidade fictícia do produto pronto; quem baixa
são os ingredientes da ficha técnica. O mesmo vale para produtos usados como adicionais.

Um produto não pode ser item de venda e insumo ao mesmo tempo. Essa separação evita
baixas duplicadas e mantém o custo do estoque compreensível.

## Operação diária

1. Se o caixa por operadora estiver ativo, a funcionária abre o caixa.
2. Em **Atendimento**, escolhe o canal, adiciona e personaliza os itens.
3. Ao tocar em **Enviar para a cozinha**, os insumos são reservados e baixados uma vez.
4. A cozinha avança o pedido e pode imprimir uma comanda térmica.
5. Ao entregar, a funcionária recebe e finaliza. A venda entra nos relatórios existentes.
6. Um cancelamento devolve os insumos; pedido já pago só pode ser cancelado por ADMIN.

## Implantação segura

Antes do primeiro deploy:

1. crie um branch de backup no Neon;
2. confirme que o Render mantém `SPRING_JPA_HIBERNATE_DDL_AUTO=validate`;
3. deixe o Flyway aplicar `V9__modulo_lanchonete.sql`;
4. confirme `/ping` e os logs sem erro de migração;
5. teste em uma loja interna antes de ativar uma cliente;
6. execute backend `./mvnw clean test` e frontend `npm run build && npm run lint && npm test`.

## Limites comerciais claros

O comprovante e a comanda são **não fiscais**. O módulo não emite NFC-e/SAT, não recebe
pedidos públicos pela internet e não integra automaticamente com iFood. Essas integrações
devem ser vendidas e implantadas separadamente, com credenciais e requisitos fiscais do
cliente.
