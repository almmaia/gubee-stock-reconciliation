# DECISIONS.md

## Interpretação do problema

O desafio pede um serviço capaz de receber eventos de estoque e pedidos, manter um saldo confiável por conta e SKU, registrar o histórico das alterações e permitir auditoria do que aconteceu com aquele saldo ao longo do tempo.

A solução precisa lidar com situações que acontecem na prática em integrações com marketplaces: eventos repetidos, eventos fora de ordem, concorrência entre requisições e recomposição de estoque pelo próprio marketplace.

## Fonte da verdade do estoque

A fonte da verdade adotada é o saldo local persistido no PostgreSQL.

Isso significa que:

- `STOCK_ADJUSTED` define o saldo absoluto do estoque.
- `ORDER_CREATED` reduz o saldo local.
- `ORDER_CANCELLED` recompõe o saldo quando o pedido já havia sido aplicado.
- `MARKETPLACE_STOCK_RESTORED` recompõe o saldo quando o marketplace devolve a disponibilidade.
- `STOCK_SYNC_SENT` registra auditoria, mas não altera o saldo local.

Essa escolha deixa a solução mais clara para consulta, auditoria e entendimento do histórico.

## Chave de controle do estoque

O controle do estoque foi modelado por `accountId + sku`.

A decisão foi usar o estoque por conta, e não por marketplace, porque o enunciado deixa claro que o mesmo SKU pode existir em contas diferentes sem afetar uma à outra. Além disso, para o desafio, faz mais sentido tratar o estoque como um saldo único por conta.

## Idempotência

A idempotência foi tratada pelo `eventId`.

Se o mesmo evento chegar mais de uma vez, ele não é reaplicado. Isso impede que uma tentativa de reenvio, falha de rede ou duplicação de transporte altere o saldo mais de uma vez.

## Duplicidade lógica

Além da idempotência por `eventId`, a solução também trata duplicidade lógica por pedido.

A mesma operação pode chegar com `eventId` diferente, mas representar o mesmo fato de negócio. Para isso, o estado do pedido é controlado por:

- `accountId`
- `marketplace`
- `externalOrderId`
- `sku`

Esse controle evita, por exemplo, que dois cancelamentos do mesmo pedido recomponham o estoque duas vezes.

## Eventos fora de ordem

O caso mais importante é `ORDER_CANCELLED` chegar antes de `ORDER_CREATED`.

Nesse cenário, a solução registra o cancelamento como pendente e não altera o saldo naquele momento. Quando o evento de criação chega depois, o estado do pedido é atualizado e o saldo permanece coerente com o fato de o pedido já ter sido cancelado antes.

Essa abordagem foi escolhida porque evita inventar saldo e mantém o histórico consistente.

## Concorrência

Dois eventos podem tentar alterar o mesmo estoque ao mesmo tempo.

Para lidar com isso, a solução usa bloqueio otimista na atualização do saldo e faz retry em caso de conflito. Antes de debitar o estoque, o saldo atual é validado para evitar que ele fique negativo por acidente.

O banco também possui uma restrição para impedir saldo negativo como segunda barreira de proteção.

## Rastreabilidade

Toda alteração relevante gera histórico.

O histórico permite responder perguntas como:

- quanto havia antes do evento
- quanto passou a existir depois
- qual foi a diferença aplicada
- qual evento causou a alteração
- quando isso aconteceu

Isso atende diretamente ao ponto de auditoria exigido no desafio.

## Comportamento com múltiplas instâncias

A aplicação pode ser executada em mais de uma instância sem perder a proteção principal da solução, porque a idempotência é garantida no banco e a concorrência é resolvida no momento da escrita.

Se houver duas instâncias processando eventos ao mesmo tempo, o banco continua sendo a camada que evita duplicidade e inconsciência no saldo.

## Trade-offs assumidos

Os principais trade-offs foram os seguintes:

- processamento síncrono via REST, em vez de Kafka real
- estado do pedido persistido para resolver duplicidade lógica
- estoque por conta e SKU, em vez de modelagem mais granular por anúncio
- `STOCK_SYNC_SENT` apenas como evento de auditoria

Essas escolhas simplificam a entrega sem perder o que o desafio realmente quer avaliar.

## Simplificações feitas por causa do prazo

Algumas partes foram mantidas simples de propósito:

- não foi criada fila real
- não foi criada autenticação
- não foi criada infraestrutura de observabilidade avançada
- não foi criado reprocessamento automático de pendências

Essas evoluções fariam sentido em produção, mas não eram necessárias para demonstrar o raciocínio principal do desafio.

## O que eu faria diferente em produção

Em um ambiente real, eu consideraria:

- Kafka com partição por chave de negócio
- dead letter queue para eventos problemáticos
- métricas e alertas
- tracing distribuído
- reprocessamento de eventos pendentes
- estratégia explícita de reconciliação com o marketplace
- autenticação e autorização nos endpoints

## Resumo da solução

A solução foi pensada para ser simples, auditável e consistente.

Ela privilegia clareza de regra de negócio, rastreabilidade e previsibilidade do saldo, que são os pontos centrais do desafio.
