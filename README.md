# gubee-stock-reconciliation

Serviço de reconciliação de estoque para integração com marketplaces, desenvolvido em Java para o desafio técnico da Gubee.

## Visão geral

A aplicação recebe eventos de estoque e pedidos via API REST, atualiza o saldo local por conta e SKU, registra o histórico das alterações e mantém o rastreio dos eventos processados, pendentes e inconsistentes.

O objetivo é manter uma visão confiável do estoque e permitir que o saldo atual possa ser explicado com base nos eventos recebidos.

## Tecnologias utilizadas

- Java 21
- Spring Boot
- PostgreSQL
- Flyway
- Arquitetura hexagonal
- Testcontainers
- OpenAPI/Swagger
- Docker Compose

## Como executar o projeto

### Com Docker Compose

```bash
docker compose up --build
```

A aplicação ficará disponível em `http://localhost:8080`.

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

### Consultar histórico do estoque

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

Os testes automatizados cobrem os cenários principais do desafio, incluindo idempotência, duplicidade lógica, eventos fora de ordem e concorrência.

## Limitações conhecidas

- O processamento é síncrono e feito via REST.
- Não há Kafka real; a solução usa processamento interno.
- O estoque é controlado por `accountId + sku`, e não por marketplace.
- O evento `STOCK_SYNC_SENT` é registrado para auditoria, mas não altera o saldo local.
- Não há autenticação nem autorização nos endpoints.
- O retry para concorrência é limitado a cinco tentativas.

## Documentação de decisões

As decisões técnicas e as justificativas estão descritas em [DECISIONS.md](DECISIONS.md).
