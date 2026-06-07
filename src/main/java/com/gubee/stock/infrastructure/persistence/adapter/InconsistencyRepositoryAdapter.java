package com.gubee.stock.infrastructure.persistence.adapter;

import com.gubee.stock.domain.model.StockEvent;
import com.gubee.stock.domain.port.outbound.InconsistencyRecord;
import com.gubee.stock.domain.port.outbound.InconsistencyRepositoryPort;
import com.gubee.stock.infrastructure.persistence.entity.InconsistencyEntity;
import com.gubee.stock.infrastructure.persistence.repository.InconsistencyJpaRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class InconsistencyRepositoryAdapter implements InconsistencyRepositoryPort {

    private final InconsistencyJpaRepository repository;

    public InconsistencyRepositoryAdapter(InconsistencyJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(StockEvent event, String reason, String payload) {
        repository.save(new InconsistencyEntity(
                event.eventId(),
                event.accountId(),
                event.sku(),
                reason,
                payload,
                event.occurredAt()
        ));
    }

    @Override
    public List<InconsistencyRecord> findAll() {
        return repository.findAll().stream()
                .map(entity -> new InconsistencyRecord(
                        entity.getEventId(),
                        entity.getAccountId(),
                        entity.getSku(),
                        entity.getReason(),
                        entity.getOccurredAt()
                ))
                .toList();
    }
}
