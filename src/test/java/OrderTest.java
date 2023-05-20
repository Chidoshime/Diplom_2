import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import praktikum.client.OrderClient;
import praktikum.client.UserClient;
import praktikum.model.Order;
import praktikum.model.User;
import praktikum.model.UserGenerator;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.is;
import static praktikum.resources.IngredientsData.*;

public class OrderTest {
    private UserClient userClient;
    private OrderClient orderClient;
    private String accessToken;
    private String[] ingredients;

    @BeforeClass
    public static void globalSetUp() {
        RestAssured.filters(
                new RequestLoggingFilter(), new ResponseLoggingFilter(),
                new AllureRestAssured()
        );
    }

    @Before
    public void setUp() {
        userClient = new UserClient();
        orderClient = new OrderClient();
    }

    @After
    public void clearData() {
        if (accessToken != null) {
            userClient.delete(accessToken);
        }
    }

    @Test
    @DisplayName("Успешное создание заказа")
    public void orderSuccessfulCreation() {
        User user = UserGenerator.getRandom();
        ingredients = orderClient.getIngredients(3);
        Order order = new Order(ingredients);

        ValidatableResponse createResponse = userClient.create(user);
        accessToken = createResponse.extract().path("accessToken");

        orderClient.createOrder(accessToken, order)
                .assertThat()
                .statusCode(SC_OK);
    }

    @Test
    @DisplayName("Cоздание заказа неавторизированным пользователем")
    public void orderCreationUnauthorizedFailed() {
        ingredients = orderClient.getIngredients(3);
        Order order = new Order(ingredients);

        orderClient.createOrderUnauthorized(order)
                .assertThat()
                .statusCode(SC_OK);
    }

    @Test
    @DisplayName("Создание заказа без ингридиентов")
    public void orderCreationWithoutIngredientsFailed() {
        User user = UserGenerator.getRandom();
        Order order = new Order(ingredients);

        ValidatableResponse createResponse = userClient.create(user);
        accessToken = createResponse.extract().path("accessToken");

        orderClient.createOrder(accessToken, order)
                .assertThat()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .assertThat()
                .body("success", is(false))
                .assertThat()
                .body("message", is("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Создание заказа с несуществующим ингридиентом")
    public void orderCreationWithNonExistentId() {
        User user = UserGenerator.getRandom();
        ingredients = new String[]{ingredientNonExistedId};
        Order order = new Order(ingredients);

        ValidatableResponse createResponse = userClient.create(user);
        accessToken = createResponse.extract().path("accessToken");

        orderClient.createOrder(accessToken, order)
                .assertThat()
                .statusCode(SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("Получение списка заказов пользователя с авторизацией")
    public void getOrdersOfUser() {
        User user = UserGenerator.getRandom();
        ingredients = orderClient.getIngredients(2);
        Order order = new Order(ingredients);

        ValidatableResponse createResponse = userClient.create(user);
        accessToken = createResponse.extract().path("accessToken");
        orderClient.createOrder(accessToken, order);

        orderClient.getOrdersOfUser(accessToken)
                .assertThat()
                .statusCode(SC_OK)
                .and()
                .assertThat()
                .body("success", is(true));
    }

    @Test
    @DisplayName("Получение списка заказов пользователя без авторизации")
    public void getUnauthorisedOrdersOfUser() {
        ingredients = orderClient.getIngredients(4);
        Order order = new Order(ingredients);

        orderClient.createOrderUnauthorized(order);

        orderClient.getUnauthorisedOrdersOfUser()
                .assertThat()
                .statusCode(SC_UNAUTHORIZED)
                .and()
                .assertThat()
                .body("success", is(false))
                .body("message", is("You should be authorised"));
    }
}