package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import io.quarkus.panache.common.Sort;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class WarehouseResourceImpl implements WarehouseResource {
  @Inject WarehouseRepository warehouseRepository;
  private Warehouse toDomain(DbWarehouse db) {
    if (db == null) return null;
    Warehouse w = new Warehouse();
    w.setId(db.businessUnitCode);
    w.setLocation(db.location);
    w.setCapacity(db.capacity);
    w.setStock(db.stock);
    return w;
  }
  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseRepository.listAll().stream().filter(dbWarehouse -> dbWarehouse.archivedAt == null)
            .map(this::toDomain)
            .collect(Collectors.toList());
  }

  @Transactional
  @Override
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    // Convert the Warehouse domain model to DbWarehouse entity
    DbWarehouse dbWarehouse = new DbWarehouse();
    dbWarehouse.businessUnitCode = data.getId();
    dbWarehouse.location = data.getLocation();
    dbWarehouse.capacity = data.getCapacity();
    dbWarehouse.stock = data.getStock();
    dbWarehouse.createdAt = java.time.LocalDateTime.now();
    dbWarehouse.archivedAt = null;

    // Persist the new warehouse into the database
    warehouseRepository.persist(dbWarehouse);

    // Return the created warehouse as a domain model
    return toDomain(dbWarehouse);
  }

  // Get a single warehouse unit by ID (business unit code)
  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    // Find the DbWarehouse entity by business unit code
    Optional<DbWarehouse> dbWarehouseOpt = warehouseRepository.find("businessUnitCode", id).firstResultOptional();

    // If not found, throw an exception or return null
    if (!dbWarehouseOpt.isPresent()) {
      throw new RuntimeException("Warehouse with business unit code " + id + " not found.");
    }

    // Convert and return the DbWarehouse as a domain model
    return toDomain(dbWarehouseOpt.get());
  }

  // Archive a warehouse unit by ID (this will set the archivedAt field)
  @Override
  @Transactional
  public void archiveAWarehouseUnitByID(String id) {
    // Find the DbWarehouse entity by business unit code
    Optional<DbWarehouse> dbWarehouseOpt = warehouseRepository.find("businessUnitCode", id).firstResultOptional();
    // If not found, throw an exception
    if (!dbWarehouseOpt.isPresent()) {
      throw new RuntimeException("Warehouse with business unit code " + id + " not found.");
    }

    // Set the archivedAt field to the current time
    DbWarehouse dbWarehouse = dbWarehouseOpt.get();
    dbWarehouse.archivedAt = java.time.LocalDateTime.now();
    // Persist the changes
    warehouseRepository.persist(dbWarehouse);
  }

}
