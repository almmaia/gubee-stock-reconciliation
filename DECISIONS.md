# DECISIONS.md

## Interpretação do problema

O serviço foi modelado para receber eventos de estoque e pedidos, manter o saldo atual por conta e SKU, registrar o histórico das alterações e permitir auditoria do que aconteceu com aquele saldo ao longo do tempo.

O desafio também exige tratamento de idempotência, duplicidade lógica, eventos fora de ordem, concorrência e rastreabilidade.

## Fonte da verdade do estoque

A fonte da verdade adotada é o saldo local persistido no PostgreSQL.

Na solução:

- `STOCK_ADJUSTED` define o saldo absoluto
- `ORDER_CREATED` reduz o saldo local
- `ORDER_CANCELLED` devolve estoque quando o pedido já havia sido aplicado
- `MARKETPLACE_STOCK_RESTORED` recompõe estoque quando o marketplace devolve a disponibilidade
- `STOCK_SYNC_SENT` é registrado somente para auditoria

## Idempotência

A idempotência foi tratada pelo `eventId`.

Se o mesmo evento for recebido novamente, ele não é aplicado duas vezes.

## Duplicidade lógica

Além do `eventId`, a solução acompanha a identidade de negócio do pedido por:

- `accountId`
- `marketplace`
- `externalOrderId`
- `sku`

Isso evita aplicação duplicada de eventos que representam a mesma operação de negócio.

## Eventos fora de ordem

Quando `ORDER_CANCELLED` chega antes de `ORDER_CREATED`, o cancelamento é mantido como pendente e não altera o saldo naquele momento.

Quando o evento de criação chega depois, o estado permanece coerente sem crédito indevido.

## Concorrência

Dois eventos podem tentar alterar o mesmo SKU ao mesmo tempo.

A atualização do estoque foi protegida no banco, com validação de saldo antes da gravação, para evitar saldo negativo acidental e manter consistência no valor final.

## Múltiplas instâncias

A solução continua consistente com mais de uma instância porque a proteção principal está no banco.

Se duas instâncias processarem eventos simultaneamente, o banco continua sendo a barreira que evita aplicar a operação de forma incorreta.

## Rastreabilidade

Cada alteração relevante gera histórico suficiente para responder:

- quanto existia antes
- quanto passou a existir depois
- qual foi a diferença
- qual evento causou a alteração
- quando a alteração aconteceu

## Chave do estoque

O estoque foi controlado por `accountId + sku`.

Essa abordagem mantém o saldo separado por conta e evita mistura entre contas diferentes para o mesmo SKU.

## Marketplace específico

O estoque foi mantido como saldo único por conta e SKU.

Essa modelagem simplifica a visão do saldo e atende ao fluxo descrito no desafio.

## Trade-offs

As decisões principais foram:

- processamento síncrono via REST
- estado do pedido persistido para resolver duplicidade lógica
- estoque por conta e SKU
- `STOCK_SYNC_SENT` apenas como auditoria

## Evolução em produção

Em um cenário real, seriam considerados:

- Kafka com partição por chave de negócio
- dead letter queue para eventos problemáticos
- métricas e alertas
- tracing distribuído
- reprocessamento de eventos pendentes
- reconciliação explícita com o marketplace
- autenticação e autorização nos endpoints

## Observações finais

O projeto cobre o fluxo principal do enunciado:

- receber eventos
- aplicar as regras de estoque
- registrar histórico
- permitir consulta do saldo atual
- permitir consulta de inconsistências

O código foi organizado em camadas para manter clareza de domínio, legibilidade e facilidade de manutenção.
