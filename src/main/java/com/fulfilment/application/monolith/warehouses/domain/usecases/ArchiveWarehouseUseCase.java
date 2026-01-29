package com.fulfilment.application.monolith.warehouses.domain.usecases;

import java.time.ZonedDateTime;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class ArchiveWarehouseUseCase implements ArchiveWarehouseOperation {

  private final WarehouseStore warehouseStore;

  public ArchiveWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  @Transactional
  public void archive(Warehouse warehouse) {
    if (warehouse == null) {
      throw new WebApplicationException("Warehouse not found", 404);
    }
    if (warehouse.archivedAt != null) {
      throw new WebApplicationException("Warehouse is already archived", 409);
    }
    warehouse.archivedAt = ZonedDateTime.now();
    warehouseStore.update(warehouse);
  }
}
