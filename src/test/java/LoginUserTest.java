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
import praktikum.client.LoginClient;
import praktikum.client.UserClient;
import praktikum.model.TestUser;
import praktikum.model.TestUserCredentialsData;
import praktikum.model.TestUserGenerator;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.is;

public class LoginUserTest {
    private UserClient userClient;
    private LoginClient loginClient;
    private String accessToken;

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
        loginClient = new LoginClient();
    }

    @After
    public void clearData() {
        if (accessToken != null) {
            userClient.delete(accessToken);
        }
    }

    @Test
    @DisplayName("Успешный логин под созданным пользователем")
    public void loginWithExistedUserIsSuccessful(){
        TestUser user = TestUserGenerator.getRandom();

        ValidatableResponse createResponse = userClient.create(user);
        accessToken = createResponse.extract().path("accessToken");

        loginClient.login(TestUserCredentialsData.from(user))
                .assertThat()
                .statusCode(SC_OK)
                .and()
                .assertThat()
                .body("success", is(true))
                .body("user.email", is(user.getEmail().toLowerCase()))
                .body("user.name", is(user.getName()));
    }

    @Test
    @DisplayName("Логин под созданным пользователем с неправильной почтой")
    public void loginWithExistedUserWithInvalidEmailFailed(){
        TestUser user = TestUserGenerator.getRandom();

        ValidatableResponse createResponse = userClient.create(user);
        accessToken = createResponse.extract().path("accessToken");
        user.setEmail("");

        loginClient.login(TestUserCredentialsData.from(user))
                .assertThat()
                .statusCode(SC_UNAUTHORIZED)
                .and()
                .assertThat()
                .body("success", is(false))
                .body("message", is("email or password are incorrect"));
    }

    @Test
    @DisplayName("Логин под созданным пользователем с неправильной почтой")
    public void loginWithExistedUserWithInvalidPasswordIsFailed(){
        TestUser user = TestUserGenerator.getRandom();

        ValidatableResponse createResponse = userClient.create(user);
        accessToken = createResponse.extract().path("accessToken");
        user.setPassword("");

        loginClient.login(TestUserCredentialsData.from(user))
                .assertThat()
                .statusCode(SC_UNAUTHORIZED)
                .and()
                .assertThat()
                .body("success", is(false))
                .body("message", is("email or password are incorrect"));
    }
}