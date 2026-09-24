# Roadmap do SaaS de estoque

Este arquivo registra as decisões de produto combinadas em 24/09/2026. O sistema continua sendo um núcleo de estoque multiempresa; recursos especializados serão módulos opcionais por loja, sem clonar o projeto.

## 1. PDV — prioridade atual

- Carrinho com vários produtos e cliente opcional.
- Desconto no pedido, pagamento por PIX, dinheiro, débito, crédito ou outro e cálculo de troco.
- Baixa atômica de estoque, proteção contra clique duplo e resposta perdida.
- Caixa, lucro e relatório do WhatsApp separados por forma de pagamento.
- Comprovante não fiscal para impressora térmica.
- Histórico com reimpressão e cancelamento administrativo; o cancelamento devolve estoque, estorna caixa e exclui a venda dos indicadores.

## 2. Entrada inteligente por XML de NF-e

- Importar o XML autorizado recebido do fornecedor; não emitir documento fiscal nesta fase.
- Identificar chave, fornecedor, número, série, datas, itens, quantidades, custos e totais.
- Conciliar itens do XML com produtos da loja antes de alterar estoque.
- Mostrar divergências de quantidade/valor, produtos não reconhecidos, lotes e validades.
- Preservar o XML original e uma trilha de quem conferiu e confirmou a entrada.

## 3. Identificação e auditoria de estoque

- SKU/código interno e código de barras EAN/GTIN opcionais por produto.
- Busca e venda por leitor USB; leitura pela câmera pode ser adicionada depois.
- Contagem de inventário com comparação entre quantidade física e sistema.
- Ajustes com motivo, responsável, data e histórico que não pode ser apagado silenciosamente.
- Alertas configuráveis de estoque mínimo, validade e produtos parados.

## 4. Modo lanchonete opcional

- Feature flag própria por loja, mantendo o estoque normal quando desligada.
- Cardápio e ficha técnica: cada venda pode consumir ingredientes automaticamente.
- Comandas/pedidos por mesa, balcão ou retirada, com observações e adicionais.
- Fila de preparo com estados: recebido, preparando, pronto, entregue e cancelado.
- Fechamento no mesmo PDV e mesmas formas de pagamento, sem duplicar caixa, clientes ou relatórios.
- Depois do MVP: impressão de pedido na cozinha, taxa de entrega e painel de produtos mais vendidos.

## Regras permanentes

- Isolamento por loja em toda consulta e gravação.
- Módulos ativados individualmente pelo superadministrador.
- Funcionária opera; administradora configura, consulta finanças e cancela.
- Ações financeiras e de estoque devem ser atômicas, idempotentes e auditáveis.
- Comprovante não fiscal deve ser identificado como tal; emissão fiscal só entra com integração fiscal própria e validação contábil.
