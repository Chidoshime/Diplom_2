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
import praktikum.model.TestOrder;
import praktikum.model.TestUser;
import praktikum.model.TestUserGenerator;

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
        TestUser user = TestUserGenerator.getRandom();
        ingredients = orderClient.getIngredients(3);
        TestOrder order = new TestOrder(ingredients);

        ValidatableResponse createResponse = userClient.create(user);
        accessToken = createResponse.extract().path("accessToken");

        orderClient.createOrder(accessToken, order)
                .assertThat()
                .statusCode(SC_OK);
    }

    @Test
    @DisplayName("Cоздание заказа неавторизированным пользователем")
    public void orderCreationUnauthorizedFailed() {
        TestUser user = TestUserGenerator.getRandom();
        ingredients = orderClient.getIngredients(3);
        TestOrder order = new TestOrder(ingredients);

        ValidatableResponse createResponse = userClient.create(user);
        accessToken = createResponse.extract().path("accessToken");

        orderClient.createOrderUnauthorized(order)
                .assertThat()
                .statusCode(SC_OK);
    }

    @Test
    @DisplayName("Создание заказа без ингридиентов")
    public void orderCreationWithoutIngredientsFailed() {
        TestUser user = TestUserGenerator.getRandom();
        TestOrder order = new TestOrder(ingredients);

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
        TestUser user = TestUserGenerator.getRandom();
        ingredients = new String[]{ingredientNonExistedId};
        TestOrder order = new TestOrder(ingredients);

        ValidatableResponse createResponse = userClient.create(user);
        accessToken = createResponse.extract().path("accessToken");

        orderClient.createOrder(accessToken, order)
                .assertThat()
                .statusCode(SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("Получение информации о созданном заказе для авторизированного пользователя")
    public void getOrdersOfUser() {
        TestUser user = TestUserGenerator.getRandom();
        ingredients = orderClient.getIngredients(2);
        TestOrder order = new TestOrder(ingredients);

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
    @DisplayName("Получение информации о созданном заказе для авторизированного пользователя")
    public void getUnauthorisedOrdersOfUser() {
        TestUser user = TestUserGenerator.getRandom();
        ingredients = orderClient.getIngredients(4);
        TestOrder order = new TestOrder(ingredients);

        ValidatableResponse createResponse = userClient.create(user);
        accessToken = createResponse.extract().path("accessToken");
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