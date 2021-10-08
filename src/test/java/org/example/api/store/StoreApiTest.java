package org.example.api.store;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.example.store.Order;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

import static io.restassured.RestAssured.given;

public class StoreApiTest {
    @BeforeClass
    public void prepare() throws IOException {
        System.getProperties().load(ClassLoader.getSystemResourceAsStream("my.properties"));
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .setBaseUri("https://petstore.swagger.io/v2/") // задаём базовый адрес каждого ресурса
                .addHeader("api_key", System.getProperty("api.key")) // задаём заголовок с токеном для авторизации
                .setAccept(ContentType.JSON) // задаём заголовок accept
                .setContentType(ContentType.JSON) // задаём заголовок content-type
                .log(LogDetail.ALL) // дополнительная инструкция полного логгирования для RestAssured
                .build(); //формирование стандартной "шапки" запроса.
        RestAssured.filters(new ResponseLoggingFilter());
    }
    @Test
    public void placeOrderTest() throws IOException {
        Order order = new Order(); // создаём экземпляр POJO объекта Order
        int id = new Random().nextInt(500000); //создание произвольного айди
        order.setId(id);
        order.setPetId(1234);
        order.setQuantity(2324);
        order.setShipDate("2021-10-08T10:33:06.920Z");
        order.setStatus("placed");
        order.setComplete(true);

        // оформление заказа на питомца:
        given()
                .body(order)
                .when()
                .post("/store/order")
                .then()
                .statusCode(200);
        //поиск оформленного заказа:
        given()
                .pathParam("orderId", id)
                .when()
                .get("/store/order/{orderId}")
                .then()
                .statusCode(200);
    }
    @Test
    public void deleteOrderTest() throws IOException {
        System.getProperties().load(ClassLoader.getSystemResourceAsStream("my.properties"));
        //удаление заказа:
        given()
                .pathParam("orderId", System.getProperty("orderId"))
                .when()
                .delete("/store/order/{orderId}")
                .then()
                .statusCode(200);
        //проверка удаления заказа:
        given()
                .pathParam("orderId", System.getProperty("orderId"))
                .when()
                .get("/store/order/{orderId}")
                .then()
                .statusCode(404);
    }
    @Test
    public void inventoryTest() {
        Map inventory =
                given()
                        .when()
                        .get("/store/inventory")
                        .then()
                        .statusCode(200)
                        .extract().body()
                        .as(Map.class);
        Assert.assertTrue(inventory.containsKey("sold"), "Inventory не содержит статус sold" );
    }
}
