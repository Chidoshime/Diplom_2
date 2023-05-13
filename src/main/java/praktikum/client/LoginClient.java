package praktikum.client;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import praktikum.client.base.StellarBurgerRestClient;
import praktikum.model.TestUserCredentialsData;

import static io.restassured.RestAssured.given;

public class LoginClient extends StellarBurgerRestClient {

    private static final String USER_LOGIN_URI = BASE_URI + "auth/login";

    @Step("Login in user {user}")
    public ValidatableResponse login(TestUserCredentialsData userCredentials) {
        return given()
                .spec(getBaseReqSpec())
                .body(userCredentials)
                .when()
                .post(USER_LOGIN_URI)
                .then();
    }
}