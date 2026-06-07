# gubee-stock-reconciliation

Serviço de reconciliação de estoque para o desafio técnico da Gubee, implementado em Java com Spring Boot e PostgreSQL.

## Visão geral

A aplicação recebe eventos de estoque e pedidos via API REST, atualiza o saldo local por conta e SKU, registra histórico das alterações e disponibiliza consultas para o estoque atual, eventos processados e inconsistências.

## Tecnologias utilizadas

- Java 21
- Spring Boot
- PostgreSQL
- Flyway
- Arquitetura hexagonal
- OpenAPI/Swagger
- Docker Compose

## Como executar o projeto

### Com Docker Compose

```bash
docker compose up --build
```

A aplicação fica disponível em `http://localhost:8080`.
O PostgreSQL do projeto sobe em `localhost:5435`.

### Localmente com PostgreSQL no Docker

```bash
docker compose up postgres -d
./gradlew bootRun
```

No Windows:

```powershell
docker compose up postgres -d
.\gradlew.bat bootRun
```

## Endpoints disponíveis

### Receber evento

`POST /events`

### Consultar estoque atual

`GET /stocks/{accountId}/{sku}`

### Consultar histórico

`GET /stocks/{accountId}/{sku}/history`

### Consultar eventos por status

`GET /events?status=PENDING`

### Consultar inconsistências

`GET /inconsistencies`

## Exemplos de requisição

### Ajuste manual de estoque

```bash
curl -X POST http://localhost:8080/events \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": "evt-001",
    "type": "STOCK_ADJUSTED",
    "occurredAt": "2026-05-28T10:00:00Z",
    "accountId": "account-001",
    "sku": "ABC-123",
    "available": 10,
    "reason": "manual_adjustment"
  }'
```

### Pedido criado

```bash
curl -X POST http://localhost:8080/events \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": "evt-002",
    "type": "ORDER_CREATED",
    "occurredAt": "2026-05-28T10:05:00Z",
    "marketplace": "MERCADO_LIVRE",
    "accountId": "account-001",
    "externalOrderId": "ML-123456",
    "sku": "ABC-123",
    "quantity": 2
  }'
```

### Consultar estoque atual

```bash
curl http://localhost:8080/stocks/account-001/ABC-123
```

## Como rodar os testes

```bash
./gradlew test
```

No Windows:

```powershell
.\gradlew.bat test
```

## Limitações conhecidas

- O processamento é síncrono e feito via REST.
- Não há Kafka real; o fluxo é processado internamente.
- O estoque é controlado por `accountId + sku`, e não por marketplace.
- O evento `STOCK_SYNC_SENT` é registrado para auditoria, mas não altera o saldo local.
- Não há autenticação nem autorização nos endpoints.
- O retry para concorrência é limitado.

## Arquivos obrigatórios

- `README.md`
- `DECISIONS.md`
- `docker-compose.yml`

## Documentação de decisões

As decisões técnicas estão descritas em [DECISIONS.md](DECISIONS.md).
