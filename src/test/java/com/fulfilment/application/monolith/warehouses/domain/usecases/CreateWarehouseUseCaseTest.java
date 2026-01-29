package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
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

class CreateWarehouseUseCaseTest {

    @Mock
    private WarehouseStore warehouseStore;  // Mock WarehouseStore

    @Mock
    private LocationGateway locationGateway;  // Mock LocationGateway

    @InjectMocks
    private CreateWarehouseUseCase createWarehouseUseCase;  // UseCase to test

    private Warehouse warehouse;
    private Location validLocation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);  // Initialize mocks

        // Set up a warehouse and a location for testing
        warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("MWH.001");
        warehouse.setLocation("AMSTERDAM");
        warehouse.setCapacity(100);
        warehouse.setStock(50);
        validLocation = new Location("AMSTERDAM",5,200);
    }

    @Test
    void testCreateWarehouse_Success() {
        // Mock the behavior of the LocationGateway and WarehouseStore
        when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(null);  // Warehouse doesn't exist
        when(locationGateway.resolveByIdentifier("AMSTERDAM")).thenReturn(validLocation);  // Valid location

        // Run the create warehouse use case
        createWarehouseUseCase.create(warehouse);

        // Verify the warehouse creation
        verify(warehouseStore, times(1)).create(warehouse);  // Ensure create method was called
        assertNotNull(warehouse.getCreationAt());  // Ensure creationAt is set
    }

    @Test
    void testCreateWarehouse_AlreadyExists() {
        // Mock the behavior of the WarehouseStore to simulate an existing warehouse
        when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(warehouse);  // Warehouse already exists

        // Run the create warehouse use case and expect an exception
        WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
            createWarehouseUseCase.create(warehouse);
        });

        // Validate the exception details
        assertEquals(409, exception.getResponse().getStatus());  // Check if status is 409 Conflict
        assertEquals("Warehouse with this business unit code already exists", exception.getMessage());
    }

    @Test
    void testCreateWarehouse_InvalidLocation() {
        // Mock the behavior of the LocationGateway to return null for an invalid location
        when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(null);  // Warehouse doesn't exist
        when(locationGateway.resolveByIdentifier("INVALID_LOCATION")).thenReturn(null);  // Invalid location

        warehouse.setLocation("INVALID_LOCATION");

        // Run the create warehouse use case and expect an exception
        WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
            createWarehouseUseCase.create(warehouse);
        });

        // Validate the exception details
        assertEquals(422, exception.getResponse().getStatus());  // Check if status is 422 Unprocessable Entity
        assertEquals("Invalid warehouse location", exception.getMessage());
    }

    @Test
    void testCreateWarehouse_StockExceedsCapacity() {
        // Set stock greater than capacity
        warehouse.setStock(150);  // Stock exceeds capacity

        // Mock the behavior of the WarehouseStore and LocationGateway
        when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(null);  // Warehouse doesn't exist
        when(locationGateway.resolveByIdentifier("AMSTERDAM")).thenReturn(validLocation);  // Valid location

        // Run the create warehouse use case and expect an exception
        WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
            createWarehouseUseCase.create(warehouse);
        });

        // Validate the exception details
        assertEquals(422, exception.getResponse().getStatus());  // Check if status is 422 Unprocessable Entity
        assertEquals("Stock cannot exceed warehouse capacity", exception.getMessage());
    }

    @Test
    void testCreateWarehouse_ExceedsMaxWarehousesAtLocation() {
        // Set up a location with a max of 5 warehouses
        Location locationWithMaxWarehouses = new Location("AMSTERDAM", 5, 200);
        warehouse.setLocation("AMSTERDAM");

        // Simulate 5 warehouses already exist at the location
        when(warehouseStore.countWarehousesAtLocation("AMSTERDAM")).thenReturn(5L); // Location has 5 warehouses already
        when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(null); // Warehouse doesn't exist
        when(locationGateway.resolveByIdentifier("AMSTERDAM")).thenReturn(locationWithMaxWarehouses); // Valid location

        // Run the create warehouse use case and expect an exception
        WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
            createWarehouseUseCase.create(warehouse);
        });

        // Validate the exception details
        assertEquals(422, exception.getResponse().getStatus());  // Check if status is 422 Unprocessable Entity
        assertEquals("Maximum number of warehouses at this location has been reached", exception.getMessage());
    }

    @Test
    void testCreateWarehouse_ExceedsMaxCapacityAtLocation() {
        // Set up a location with a max total capacity of 200
        Location locationWithMaxCapacity = new Location("AMSTERDAM", 5, 200);
        warehouse.setLocation("AMSTERDAM");
        warehouse.setCapacity(150); // Warehouse has capacity of 150

        // Simulate existing warehouses' total capacity of 100
        when(warehouseStore.sumWarehouseCapacitiesAtLocation("AMSTERDAM")).thenReturn(100); // Current capacity = 100
        when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(null); // Warehouse doesn't exist
        when(locationGateway.resolveByIdentifier("AMSTERDAM")).thenReturn(locationWithMaxCapacity); // Valid location

        // Run the create warehouse use case and expect an exception
        WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
            createWarehouseUseCase.create(warehouse);
        });

        // Validate the exception details
        assertEquals(422, exception.getResponse().getStatus());  // Check if status is 422 Unprocessable Entity
        assertEquals("Total capacity at this location exceeds the allowed limit", exception.getMessage());
    }

    @Test
    void testCreateWarehouse_NullLocation() {
        // Set warehouse location to null
        warehouse.setLocation(null);

        // Simulate behavior for null location
        when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(null); // Warehouse doesn't exist

        // Run the create warehouse use case and expect an exception
        WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
            createWarehouseUseCase.create(warehouse);
        });

        // Validate the exception details
        assertEquals(422, exception.getResponse().getStatus());  // Check if status is 422 Unprocessable Entity
        assertEquals("Invalid warehouse location", exception.getMessage());
    }
}
