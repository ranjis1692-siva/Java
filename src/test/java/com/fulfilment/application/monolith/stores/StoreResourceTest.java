package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class StoreResourceTest {

    @Test
    public void testGetStoreById() {
        given()
                .pathParam("id", 1)
                .when().get("/stores/{id}")
                .then()
                .statusCode(200)
                .body("id", is(1));
    }

    @Test
    public void testCreateStore() {
        String json = "{ \"name\": \"Main Store\", \"location\": \"Rotterdam\" }";

        given()
                .contentType("application/json")
                .body(json)
                .when().post("/stores")
                .then()
                .statusCode(201)
                .body("name", is("Main Store"));
    }
}
