package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import io.quarkus.test.junit.QuarkusTest;
import io.rest-assured.RestAssured;
import org.junit.jupiter.api.Test;

import static io.rest-assured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class WarehouseResourceImplTest {

    @Test
    public void testGetAllWarehouses() {
        given()
                .when().get("/warehouses")
                .then()
                .statusCode(200)
                .body("$", not(empty()));
    }

    @Test
    public void testCreateWarehouse() {
        String json = "{ \"name\": \"Test Warehouse\", \"location\": \"Amsterdam\" }";

        given()
                .contentType("application/json")
                .body(json)
                .when().post("/warehouses")
                .then()
                .statusCode(201)
                .body("name", is("Test Warehouse"))
                .body("location", is("Amsterdam"));
    }

    @Test
    public void testCreateWarehouseValidationFail() {
        String json = "{ \"name\": \"\", \"location\": \"\" }";

        given()
                .contentType("application/json")
                .body(json)
                .when().post("/warehouses")
                .then()
                .statusCode(400);
    }
}
