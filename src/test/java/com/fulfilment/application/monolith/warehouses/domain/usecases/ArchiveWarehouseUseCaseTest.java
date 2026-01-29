package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.ZonedDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ArchiveWarehouseUseCaseTest {

    private WarehouseStore warehouseStoreMock;
    private ArchiveWarehouseUseCase archiveWarehouseUseCase;

    @BeforeEach
    public void setup() {
        warehouseStoreMock = Mockito.mock(WarehouseStore.class);
        archiveWarehouseUseCase = new ArchiveWarehouseUseCase(warehouseStoreMock);
    }

    @Test
    public void testArchiveWarehouseSuccessfully() {
        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("MWH.001");
        warehouse.setLocation("ZWOLLE-001");
        warehouse.setCapacity(100);
        warehouse.setStock(10);
        warehouse.setCreationAt(ZonedDateTime.now());
        warehouse.setArchivedAt(null);
        archiveWarehouseUseCase.archive(warehouse);
        assert warehouse.getArchivedAt() != null;
        verify(warehouseStoreMock, times(1)).update(any(Warehouse.class));
    }

    @Test
    public void testArchiveWarehouseAlreadyArchived() {
        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("MWH.001");
        warehouse.setLocation("ZWOLLE-001");
        warehouse.setCapacity(100);
        warehouse.setStock(10);
        warehouse.setCreationAt(ZonedDateTime.now());
        warehouse.setArchivedAt(ZonedDateTime.now());  // Already archived
        try {
            archiveWarehouseUseCase.archive(warehouse);
            assert false : "Expected WebApplicationException to be thrown.";
        } catch (WebApplicationException e) {
            assert e.getResponse().getStatus() == 409;  // Conflict status, already archived
        }
        verify(warehouseStoreMock, times(0)).update(any(Warehouse.class));
    }

    @Test
    public void testArchiveWarehouseNotFound() {
        Warehouse warehouse = null;
        try {
            archiveWarehouseUseCase.archive(warehouse);
            assert false : "Expected WebApplicationException to be thrown.";
        } catch (WebApplicationException e) {
            assert e.getResponse().getStatus() == 404;  // Not found status
        }
        verify(warehouseStoreMock, times(0)).update(any(Warehouse.class));
    }
}
