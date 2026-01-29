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
  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationGateway locationGateway) {
    this.warehouseStore = warehouseStore;
    this.locationGateway = locationGateway;
  }

  @Override
  public void create(Warehouse warehouse) {
    // Business unit code check
    if (warehouseStore.findByBusinessUnitCode(warehouse.getBusinessUnitCode()) != null) {
      throw new WebApplicationException("Warehouse with this business unit code already exists", 409);
    }

    // Location validation
    Location location = locationGateway.resolveByIdentifier(warehouse.getLocation());
    if (location == null) {
      throw new WebApplicationException("Invalid warehouse location", 422);
    }

    // Max warehouse count validation
    int currentWarehouseCount = (int) warehouseStore.countWarehousesAtLocation(warehouse.getLocation());
    if (currentWarehouseCount >= location.maxNumberOfWarehouses) {
      throw new WebApplicationException("Maximum number of warehouses at this location has been reached", 422);
    }

    // Max capacity validation
    int totalCapacityAtLocation = warehouseStore.sumWarehouseCapacitiesAtLocation(warehouse.getLocation());
    if (totalCapacityAtLocation + warehouse.getCapacity() > location.maxCapacity) {
      throw new WebApplicationException("Total capacity at this location exceeds the allowed limit", 422);
    }

    // Capacity and stock validation
    if (warehouse.getStock() > warehouse.getCapacity()) {
      throw new WebApplicationException("Stock cannot exceed warehouse capacity", 422);
    }

    // Set creation time and persist the warehouse
    warehouse.setCreationAt(ZonedDateTime.now());
    warehouseStore.create(warehouse);
  }
}
