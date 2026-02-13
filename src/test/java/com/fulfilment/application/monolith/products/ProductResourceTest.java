package com.fulfilment.application.monolith.products;

import io.quarkus.test.junit.QuarkusTest;
import io.rest-assured.RestAssured;
import org.junit.jupiter.api.Test;

import static io.rest-assured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class ProductResourceTest {

    @Test
    public void testGetProductById() {
        given()
                .pathParam("id", 1)
                .when().get("/products/{id}")
                .then()
                .statusCode(200)
                .body("id", is(1));
    }

    @Test
    public void testCreateProduct() {
        String json = "{ \"name\": \"Laptop\", \"price\": 1200.0 }";

        given()
                .contentType("application/json")
                .body(json)
                .when().post("/products")
                .then()
                .statusCode(201)
                .body("name", is("Laptop"));
    }

    @Test
    public void testProductErrorMapper() {
        given()
                .pathParam("id", 9999) // non-existing product
                .when().get("/products/{id}")
                .then()
                .statusCode(404); // triggers ProductResource.ErrorMapper
    }
}
