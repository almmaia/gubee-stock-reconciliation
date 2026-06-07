package com.gubee.stock.domain.port.inbound;

import com.gubee.stock.domain.port.outbound.InconsistencyRecord;
import java.util.List;

public interface GetInconsistenciesUseCase {
    List<InconsistencyRecord> getAll();
}
