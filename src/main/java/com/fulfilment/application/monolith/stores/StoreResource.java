package com.fulfilment.application.monolith.stores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.List;
import org.jboss.logging.Logger;
import jakarta.enterprise.inject.spi.CDI;

@Path("stores")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class StoreResource {
  @Inject LegacyStoreManagerGateway legacyStoreManagerGateway;
  private static final Logger LOGGER = Logger.getLogger(StoreResource.class.getName());

  @GET
  public List<Store> get() {
    return Store.listAll(Sort.by("name"));
  }

  @GET
  @Path("{id}")
  public Store getSingle(Long id) {
    Store entity = Store.findById(id);
    if (entity == null) {
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }
    return entity;
  }

  @POST
  @Transactional
  public Response create(Store store) {
    if (store.id != null) {
      throw new WebApplicationException("Id was invalidly set on request.", 422);
    }
    store.persist();
    runAfterCommit(() -> legacyStoreManagerGateway.createStoreOnLegacySystem(store));
    return Response.ok(store).status(201).build();
  }

  @PUT
  @Path("{id}")
  @Transactional
  public Store update(Long id, Store updatedStore) {
    if (updatedStore.name == null) {
      throw new WebApplicationException("Store Name was not set on request.", 422);
    }
    Store entity = Store.findById(id);
    if (entity == null) {
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }
    entity.name = updatedStore.name;
    entity.quantityProductsInStock = updatedStore.quantityProductsInStock;
    runAfterCommit(() -> legacyStoreManagerGateway.updateStoreOnLegacySystem(entity));
    return entity;
  }

  @PATCH
  @Path("{id}")
  @Transactional
  public Store patch(Long id, Store updatedStore) {
    Store entity = Store.findById(id);
    if (entity == null) {
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }
    if (updatedStore.name != null) {
      entity.name = updatedStore.name;
    }
    if (updatedStore.quantityProductsInStock != null) {
      entity.quantityProductsInStock = updatedStore.quantityProductsInStock;
    }
    runAfterCommit(() -> legacyStoreManagerGateway.updateStoreOnLegacySystem(entity));
    return entity;
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(Long id) {
    Store entity = Store.findById(id);
    if (entity == null) {
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }
    entity.delete();
    return Response.status(204).build();
  }

  private void runAfterCommit(Runnable task) {
    TransactionSynchronizationRegistry registry =
            CDI.current().select(TransactionSynchronizationRegistry.class).get();
    registry.registerInterposedSynchronization(new Synchronization() {
      @Override
      public void beforeCompletion() {
      }
      @Override
      public void afterCompletion(int status) {
        if (status == Status.STATUS_COMMITTED) {
          try {
            task.run();
          } catch (Exception e) {
            LOGGER.error("Failed to execute after-commit task", e);
          }
        }
      }
    });
  }

  @Provider
  public static class ErrorMapper implements ExceptionMapper<Exception> {
    @Inject ObjectMapper objectMapper;
    @Override
    public Response toResponse(Exception exception) {
      LOGGER.error("Failed to handle request", exception);
      int code = 500;
      if (exception instanceof WebApplicationException) {
        code = ((WebApplicationException) exception).getResponse().getStatus();
      }
      ObjectNode exceptionJson = objectMapper.createObjectNode();
      exceptionJson.put("exceptionType", exception.getClass().getName());
      exceptionJson.put("code", code);
      if (exception.getMessage() != null) {
        exceptionJson.put("error", exception.getMessage());
      }
      return Response.status(code).entity(exceptionJson).build();
    }
  }
}
