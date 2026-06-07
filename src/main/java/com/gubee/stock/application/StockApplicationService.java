package com.gubee.stock.application;

import com.gubee.stock.domain.model.EventProcessingResult;
import com.gubee.stock.domain.model.EventProcessingStatus;
import com.gubee.stock.domain.model.StockEvent;
import com.gubee.stock.domain.model.StockHistoryEntry;
import com.gubee.stock.domain.model.StockSnapshot;
import com.gubee.stock.domain.port.inbound.GetEventsByStatusUseCase;
import com.gubee.stock.domain.port.inbound.GetInconsistenciesUseCase;
import com.gubee.stock.domain.port.inbound.GetStockHistoryUseCase;
import com.gubee.stock.domain.port.inbound.GetStockUseCase;
import com.gubee.stock.domain.port.inbound.ProcessEventUseCase;
import com.gubee.stock.domain.port.outbound.InconsistencyRecord;
import com.gubee.stock.domain.port.outbound.InconsistencyRepositoryPort;
import com.gubee.stock.domain.port.outbound.OrderStateRepositoryPort;
import com.gubee.stock.domain.port.outbound.ProcessedEventRecord;
import com.gubee.stock.domain.port.outbound.ProcessedEventRepositoryPort;
import com.gubee.stock.domain.port.outbound.StockRepositoryPort;
import com.gubee.stock.domain.service.StockEventProcessor;
import java.util.List;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockApplicationService implements ProcessEventUseCase,
        GetStockUseCase,
        GetStockHistoryUseCase,
        GetEventsByStatusUseCase,
        GetInconsistenciesUseCase {

    private static final int MAX_RETRIES = 5;

    private final StockEventProcessor processor;
    private final StockRepositoryPort stockRepository;
    private final ProcessedEventRepositoryPort processedEventRepository;
    private final InconsistencyRepositoryPort inconsistencyRepository;

    public StockApplicationService(
            StockRepositoryPort stockRepository,
            ProcessedEventRepositoryPort processedEventRepository,
            OrderStateRepositoryPort orderStateRepository,
            InconsistencyRepositoryPort inconsistencyRepository
    ) {
        this.stockRepository = stockRepository;
        this.processedEventRepository = processedEventRepository;
        this.inconsistencyRepository = inconsistencyRepository;
        this.processor = new StockEventProcessor(
                stockRepository,
                processedEventRepository,
                orderStateRepository,
                inconsistencyRepository
        );
    }

    @Override
    @Transactional
    public EventProcessingResult process(StockEvent event, String payload) {
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                return processor.process(event, payload);
            } catch (OptimisticLockingFailureException ex) {
                if (attempt == MAX_RETRIES - 1) {
                    throw ex;
                }
            }
        }
        throw new IllegalStateException("Unreachable");
    }

    @Override
    @Transactional(readOnly = true)
    public StockSnapshot getStock(String accountId, String sku) {
        return stockRepository.findStock(accountId, sku);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockHistoryEntry> getHistory(String accountId, String sku) {
        return stockRepository.findHistory(accountId, sku);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcessedEventRecord> getByStatus(EventProcessingStatus status) {
        return processedEventRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InconsistencyRecord> getAll() {
        return inconsistencyRepository.findAll();
    }
}
