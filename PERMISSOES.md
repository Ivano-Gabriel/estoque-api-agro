# Matriz de permissões

| Ação | Funcionária | Administradora | Superadmin |
| --- | --- | --- | --- |
| Consultar catálogo e clientes | Sim | Sim | Não acessa dados da loja |
| Registrar venda e cliente | Sim | Sim | Não |
| Editar cliente | Sim | Sim | Não |
| Criar, editar, arquivar ou restaurar produto | Não | Sim | Não |
| Repor estoque e importar planilha | Não | Sim | Não |
| Consultar financeiro, auditoria e notas recebidas | Não | Sim | Não |
| Cancelar venda, devolver e reembolsar | Não | Sim | Não |
| Abrir e fechar o próprio caixa | Sim | Sim | Não |
| Fazer sangria/suprimento e conferir outros caixas | Não | Sim | Não |
| Criar/bloquear funcionárias | Não | Sim | Não |
| Criar, bloquear e configurar lojas | Não | Não | Sim, com MFA |

Mudanças futuras de permissão devem alterar backend, frontend e testes no mesmo pull request. Esconder um botão não substitui autorização no servidor.
