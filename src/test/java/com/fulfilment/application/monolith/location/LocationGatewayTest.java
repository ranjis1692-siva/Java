package com.fulfilment.application.monolith.location;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LocationGatewayTest {

  @Test
  public void testWhenResolveExistingLocationShouldReturn() {
    LocationGateway locationGateway = new LocationGateway();
    Location result = locationGateway.resolveByIdentifier("ZWOLLE-001");
    assertEquals("ZWOLLE-001", result.identification);
    assertEquals(1, result.maxNumberOfWarehouses);
    assertEquals(40, result.maxCapacity);
  }

  @Test
  public void testWhenResolveNonExistingLocationShouldReturn() {
    LocationGateway locationGateway = new LocationGateway();
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      locationGateway.resolveByIdentifier("NONEXISTENT-001");
    });
    assertEquals("Location with identifier 'NONEXISTENT-001' not found", exception.getMessage());
  }
}
