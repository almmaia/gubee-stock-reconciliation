package com.gubee.stock.infrastructure.persistence.adapter;

import com.gubee.stock.domain.model.EventProcessingStatus;
import com.gubee.stock.domain.model.StockEvent;
import com.gubee.stock.domain.port.outbound.ProcessedEventRecord;
import com.gubee.stock.domain.port.outbound.ProcessedEventRepositoryPort;
import com.gubee.stock.infrastructure.persistence.entity.ProcessedEventEntity;
import com.gubee.stock.infrastructure.persistence.repository.ProcessedEventJpaRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ProcessedEventRepositoryAdapter implements ProcessedEventRepositoryPort {

    private final ProcessedEventJpaRepository repository;

    public ProcessedEventRepositoryAdapter(ProcessedEventJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsByEventId(String eventId) {
        return repository.existsById(eventId);
    }

    @Override
    public void save(StockEvent event, EventProcessingStatus status, String message, String payload) {
        repository.save(new ProcessedEventEntity(
                event.eventId(),
                event.type().name(),
                event.accountId(),
                event.sku(),
                status.name(),
                message,
                payload,
                event.occurredAt()
        ));
    }

    @Override
    public List<ProcessedEventRecord> findByStatus(EventProcessingStatus status) {
        return repository.findByStatus(status.name()).stream()
                .map(entity -> new ProcessedEventRecord(
                        entity.getEventId(),
                        entity.getEventType(),
                        entity.getAccountId(),
                        entity.getSku(),
                        EventProcessingStatus.valueOf(entity.getStatus()),
                        entity.getResultMessage(),
                        entity.getOccurredAt()
                ))
                .toList();
    }
}
