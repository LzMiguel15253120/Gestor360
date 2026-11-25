# Gestor360

Sistema web de gestão para pequenas empresas. A aplicação usa uma API Spring Boot com SQLite e um cliente React servido em desenvolvimento pelo Vite. A interface não carrega registros de demonstração: listas vazias indicam que ainda não há dados cadastrados.

## Requisitos e versões

- Java 25 (LTS) ou superior
- Maven 3.9 ou superior (recomendado)
- Node.js 20.19+ ou 22.12+ e npm
- Dependências do frontend instaladas pelo npm

Versões principais definidas no projeto: Spring Boot 3.5.7, React 19 e Vite 7. O frontend usa JavaScript e CSS sem framework adicional. O backend usa Spring Web, Spring Data JPA, Hibernate, Hibernate Community Dialects para SQLite, SQLite JDBC e Jakarta Validation. Os testes usam JUnit 5, Mockito e Spring Boot Test.

## Executar localmente

Abra dois terminais na pasta do projeto. No primeiro, inicie a API:

```powershell
cd backend
mvn spring-boot:run
```

No segundo, instale as dependências e inicie o frontend:

```powershell
cd frontend
npm install
npm run dev
```

Abra <http://localhost:5173>. O Vite encaminha chamadas `/api` para `http://localhost:8080`. O endpoint `/api/health` mostra o estado da conexão na navegação.

## Banco de dados e configuração

Por padrão, o backend cria/usa `backend/data/gestor360.db` quando iniciado a partir da pasta `backend`. A URL SQLite e o caminho alternativo ficam em `backend/src/main/resources/application.properties`. Para apontar para outro arquivo, configure `GESTOR360_DB_PATH` antes de iniciar a aplicação; no PowerShell, por exemplo:

```powershell
$env:GESTOR360_DB_PATH = 'D:/dados/gestor360.db'
cd backend
mvn spring-boot:run
```

Hibernate usa `ddl-auto=update` para criar e atualizar o esquema durante o desenvolvimento. O pool SQLite liga `PRAGMA foreign_keys=ON` em cada conexão. Valores monetários são enviados e armazenados como inteiros em centavos; os formulários exibem reais no padrão pt-BR. Datas de relatório usam o fuso local do servidor.

O frontend usa `frontend/vite.config.js` para o proxy local. O backend permite CORS de `http://localhost:5173` para `/api/**`.

## Recursos implementados

- **Cadastros:** criar, listar, editar e excluir categorias, clientes e fornecedores. A exclusão é recusada quando há referências operacionais. Clientes têm consulta do histórico de vendas.
- **Produtos:** criar, listar, editar e desativar. Produtos usados em operações não são apagados fisicamente; produtos inativos podem ser reativados. O estoque inicial é zero e muda por operações de compra/venda.
- **Vendas:** registrar itens com preço atual do produto, consumidor final ou cliente, pagamento imediato ou a prazo, histórico e cancelamento. A venda verifica e baixa o estoque na mesma transação. O prazo exige cliente e vencimento.
- **Compras:** registrar pedido pendente com custo por item e fornecedor. O estoque e a conta a pagar só são atualizados quando a compra inteira é recebida.
- **Estoque:** posição atual, alerta por estoque mínimo e até 100 movimentações recentes. As entradas e saídas operacionais ficam ligadas à venda ou compra de origem.
- **Financeiro:** contas a receber de vendas e contas a pagar de compras recebidas, pagamentos parciais, vencimentos e histórico por conta.
- **Painel:** vendas de hoje, valores recebidos/pagos no dia, estoque baixo, contas pendentes e vendas recentes.
- **Relatórios:** período de até 367 dias, total e lista de vendas, compras do período, recebimentos/pagamentos, produtos mais vendidos e posição atual de estoque baixo.
- **API:** validação de entrada, respostas HTTP REST, mensagens centralizadas e transações para vendas, cancelamentos, recebimentos e pagamentos.

### Rotas principais

| Área | Rotas |
| --- | --- |
| Estado da API | `GET /api/health` |
| Produtos e categorias | `/api/produtos`, `/api/categorias` |
| Clientes e fornecedores | `/api/clientes`, `/api/fornecedores` |
| Vendas e compras | `/api/vendas`, `/api/compras`; `POST /api/vendas/{id}/cancelar`; `POST /api/compras/{id}/receber` |
| Estoque e financeiro | `GET /api/estoque/movimentacoes`, `/api/financeiro/contas`, `/api/financeiro/contas/{id}/pagamentos` |
| Painel e relatórios | `GET /api/dashboard`, `GET /api/relatorios?inicio=AAAA-MM-DD&fim=AAAA-MM-DD` |

Valores monetários na API usam centavos inteiros. Para criar uma venda imediata, `paymentMethod` aceita `PIX`, `DINHEIRO`, `CARTAO_CREDITO`, `CARTAO_DEBITO` ou `TRANSFERENCIA`. Para venda a prazo, use `A_PRAZO` e informe `customerId` e `dueDate`. Cada produto deve aparecer uma vez por operação.

## Regras e limitações atuais

- O recebimento de compras é integral; não há recebimento parcial nem cancelamento de compra na interface.
- O cancelamento de venda devolve estoque, mas só é permitido quando a conta ainda não tem pagamentos. Estorno de vendas pagas/parcialmente pagas ainda precisa de um fluxo financeiro explícito.
- Não há descontos de venda. O pedido original prevê descontos sujeitos a permissão, mas a matriz de autorização ainda não foi definida.
- A autenticação e as permissões não estão implementadas. O requisito original define ADMIN, GERENTE e FUNCIONARIO, BCrypt e JWT, porém ainda precisa decidir como o primeiro ADMIN será criado, se haverá cadastro público e quais ações cabem a cada perfil. Proposta compatível: Spring Security com JWT, criação inicial de ADMIN por configuração segura e uma matriz de permissões aprovada antes de liberar acessos.
- Operações importantes ainda não têm usuário responsável porque o sistema de login não existe. Não use esta API como serviço multiusuário exposto em rede ou produção até implementar autenticação, autorização e auditoria.
- A estrutura usa atualização automática de esquema para desenvolvimento; ainda não há migrações versionadas.
- Não há importação/exportação, integração externa, anexos ou dashboard com gráficos históricos.

## Testes e build

Backend (inclui teste de inicialização e testes unitários das regras de venda):

```powershell
cd backend
mvn test
```

Frontend:

```powershell
cd frontend
npm run build
```

O teste de contexto inicializa o SQLite configurado pela aplicação. O frontend deve ser construído com Node.js dentro das versões suportadas pelo Vite.
