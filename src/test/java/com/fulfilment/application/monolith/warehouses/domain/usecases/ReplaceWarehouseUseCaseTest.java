package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.ZonedDateTime;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ReplaceWarehouseUseCaseTest {

    @Mock
    private WarehouseStore warehouseStore;  // Mock WarehouseStore

    @InjectMocks
    private ReplaceWarehouseUseCase replaceWarehouseUseCase;  // UseCase to test

    private Warehouse existingWarehouse;
    private Warehouse newWarehouse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);  // Initialize mocks
        existingWarehouse = new Warehouse();
        existingWarehouse.setBusinessUnitCode("MWH.001");
        existingWarehouse.setLocation("AMSTERDAM");
        existingWarehouse.setCapacity(100);
        existingWarehouse.setStock(50);
        existingWarehouse.setCreationAt(ZonedDateTime.now());
        existingWarehouse.setArchivedAt(null);
        newWarehouse = new Warehouse();
        newWarehouse.setBusinessUnitCode("MWH.001");
        newWarehouse.setLocation("AMSTERDAM");
        newWarehouse.setCapacity(100);
        newWarehouse.setStock(50);
    }

    @Test
        void testReplaceWarehouse_Success() {
            when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existingWarehouse);  // Existing warehouse
            doNothing().when(warehouseStore).update(existingWarehouse);  // Simulate update success (void method)
            doNothing().when(warehouseStore).create(newWarehouse);  // Simulate new warehouse creation (void method)
            replaceWarehouseUseCase.replace(newWarehouse);
            verify(warehouseStore, times(1)).update(existingWarehouse);  // Ensure update method was called
            verify(warehouseStore, times(1)).create(newWarehouse);  // Ensure create method was called
            assertNotNull(existingWarehouse.getArchivedAt());  // Ensure the existing warehouse is archived
            assertNull(newWarehouse.getArchivedAt());  // Ensure the new warehouse is not archived
    }

    @Test
    void testReplaceWarehouse_WarehouseNotExist() {
        when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(null);  // Warehouse does not exist
        WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
            replaceWarehouseUseCase.replace(newWarehouse);
        });
        assertEquals(404, exception.getResponse().getStatus());  // Check if status is 404 Not Found
        assertEquals("Warehouse to be replaced does not exist", exception.getMessage());
    }

    @Test
    void testReplaceWarehouse_ArchivedWarehouse() {
        existingWarehouse.setArchivedAt(ZonedDateTime.now());  // Set archivedAt to simulate archived warehouse
        when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existingWarehouse);  // Archived warehouse
        WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
            replaceWarehouseUseCase.replace(newWarehouse);
        });
        assertEquals(409, exception.getResponse().getStatus());  // Check if status is 409 Conflict
        assertEquals("Cannot replace an archived warehouse", exception.getMessage());
    }

    @Test
    void testReplaceWarehouse_StockMismatch() {
        newWarehouse.setStock(60);  // New stock does not match existing stock
        when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existingWarehouse);  // Existing warehouse
        WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
            replaceWarehouseUseCase.replace(newWarehouse);
        });
        assertEquals(422, exception.getResponse().getStatus());  // Check if status is 422 Unprocessable Entity
        assertEquals("New warehouse stock must match existing warehouse stock", exception.getMessage());
    }

    @Test
    void testReplaceWarehouse_InsufficientCapacity() {
        newWarehouse.setCapacity(40);  // New capacity cannot accommodate existing stock
        when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existingWarehouse);  // Existing warehouse
        WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
            replaceWarehouseUseCase.replace(newWarehouse);
        });
        assertEquals(422, exception.getResponse().getStatus());  // Check if status is 422 Unprocessable Entity
        assertEquals("New warehouse capacity cannot accommodate existing stock", exception.getMessage());
    }
}
