package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import java.time.ZonedDateTime;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    Warehouse existing =
            warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (existing == null) {
      throw new WebApplicationException(
              "Warehouse to be replaced does not exist", 404);
    }
    if (existing.archivedAt != null) {
      throw new WebApplicationException(
              "Cannot replace an archived warehouse", 409);
    }
    if (!existing.stock.equals(newWarehouse.stock)) {
      throw new WebApplicationException(
              "New warehouse stock must match existing warehouse stock", 422);
    }
    if (newWarehouse.capacity < existing.stock) {
      throw new WebApplicationException(
              "New warehouse capacity cannot accommodate existing stock", 422);
    }
    existing.archivedAt = ZonedDateTime.now();
    warehouseStore.update(existing);
    newWarehouse.creationAt = ZonedDateTime.now();
    newWarehouse.archivedAt = null;
    warehouseStore.create(newWarehouse);
  }
}
