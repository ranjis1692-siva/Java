package com.fulfilment.application.monolith.warehouses.domain.usecases;
import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import java.time.ZonedDateTime;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationGateway locationGateway;

  @Inject
  public CreateWarehouseUseCase(
          WarehouseStore warehouseStore,
          LocationGateway locationGateway) {
    this.warehouseStore = warehouseStore;
    this.locationGateway = locationGateway;
  }
  @Override
  public void create(Warehouse warehouse) {
    if (warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode) != null) {
      throw new WebApplicationException(
              "Warehouse with this business unit code already exists", 409);
    }
    Location location = locationGateway.resolveByIdentifier(warehouse.location);
    if (location == null) {
      throw new WebApplicationException("Invalid warehouse location", 422);
    }
    if (warehouse.stock > warehouse.capacity) {
      throw new WebApplicationException(
              "Stock cannot exceed warehouse capacity", 422);
    }
    warehouse.creationAt = ZonedDateTime.now();
    warehouseStore.create(warehouse);
  }

}
