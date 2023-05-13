package praktikum.client;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import praktikum.client.base.StellarBurgerRestClient;
import praktikum.model.TestOrder;

import java.util.List;
import java.util.Random;

import static io.restassured.RestAssured.given;

public class OrderClient extends StellarBurgerRestClient {

    private static final String ORDER_BASE_URL = BASE_URI + "orders";
    private static final String INGREDIENT_BASE_URL = BASE_URI + "ingredients";
    Random rand = new Random();
    @Step("Create order")
    public ValidatableResponse createOrder(String accessToken, TestOrder order){
        return given()
                .spec(getBaseReqSpec())
                .header("Authorization", accessToken)
                .body(order)
                .when()
                .post(ORDER_BASE_URL)
                .then();
    }

    @Step("Create order")
    public ValidatableResponse createOrderUnauthorized(TestOrder order){
        return given()
                .spec(getBaseReqSpec())
                .body(order)
                .when()
                .post(ORDER_BASE_URL)
                .then();
    }

    @Step("Get orders of user")
    public ValidatableResponse getOrdersOfUser(String accessToken){
        return given()
                .spec(getBaseReqSpec())
                .header("Authorization", accessToken)
                .when()
                .get(ORDER_BASE_URL)
                .then();
    }

    @Step("Get orders of user")
    public ValidatableResponse getUnauthorisedOrdersOfUser(){
        return given()
                .spec(getBaseReqSpec())
                .when()
                .get(ORDER_BASE_URL)
                .then();
    }

    @Step("get ingredients list")
    public String[] getIngredients(int count){
        Response ingredients = given()
                .spec(getBaseReqSpec())
                .when()
                .get(INGREDIENT_BASE_URL);

        List<String> ingredientsList = ingredients.getBody().jsonPath().getList("data._id");
        String[] ingredientsOutput = new String[count];
        for(int i = 0; i < ingredientsOutput.length; i++){
            ingredientsOutput[i] = ingredientsList.get(rand.nextInt(ingredientsList.size()));
        }
        return ingredientsOutput;
    }
}