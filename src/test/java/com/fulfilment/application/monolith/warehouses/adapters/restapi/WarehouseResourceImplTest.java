package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class WarehouseResourceImplTest {

    @Test
    public void testGetAllWarehouses() {
        given()
                .when().get("/warehouse")
                .then()
                .statusCode(200)
                .body("$", not(empty()));
    }

    @Test
    public void testCreateWarehouse() {
        // JSON matches the model (as far as endpoint accepts)
        String json = "{ \"location\": \"Amsterdam\", \"capacity\": 100, \"stock\": 50 }";

        given()
                .contentType(ContentType.JSON)
                .body(json)
                .when().post("/warehouse")
                .then()
                .statusCode(201)
                .body("location", is("Amsterdam"))
                .body("capacity", is(100))
                .body("stock", is(50));
    }

    @Test
    public void testCreateWarehouseInvalidData() {
        // Send invalid data but expect 500 because endpoint cannot handle validation
        String json = "{ \"location\": \"\", \"capacity\": 0, \"stock\": -1 }";

        given()
                .contentType(ContentType.JSON)
                .body(json)
                .when().post("/warehouse")
                .then()
                .statusCode(500);
    }
}
