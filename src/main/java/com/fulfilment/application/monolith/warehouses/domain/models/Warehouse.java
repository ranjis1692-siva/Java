package com.fulfilment.application.monolith.warehouses.domain.models;

import java.time.ZonedDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Warehouse {

  // unique identifier
  public String businessUnitCode;

  public String location;

  public Integer capacity;

  public Integer stock;

  public ZonedDateTime creationAt;

  public ZonedDateTime archivedAt;
}
