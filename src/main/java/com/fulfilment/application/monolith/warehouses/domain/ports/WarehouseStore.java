package com.fulfilment.application.monolith.warehouses.domain.ports;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;

public interface WarehouseStore {
  void create(Warehouse warehouse);

  void update(Warehouse warehouse);

  void remove(Warehouse warehouse);

  Warehouse findByBusinessUnitCode(String buCode);

  long countWarehousesAtLocation(String location);

  // Method to sum warehouse capacities at a specific location
  int sumWarehouseCapacitiesAtLocation(String location);
}
