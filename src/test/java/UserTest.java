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
import praktikum.client.UserClient;
import praktikum.model.TestUser;
import praktikum.model.TestUserGenerator;
import praktikum.model.TestUserUpdateEmail;
import praktikum.model.TestUserUpdateName;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

public class UserTest {
    private UserClient userClient;
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
    }

    @After
    public void clearData() {
        if (accessToken != null) {
            userClient.delete(accessToken);
        }
    }

    @Test
    @DisplayName("Создание пользователя с валидными данными")
    public void userCanBeCreatedWithValidData() {
        TestUser user = TestUserGenerator.getRandom();

        ValidatableResponse createResponse = userClient.create(user);
        accessToken = createResponse.extract().path("accessToken");

        createResponse.assertThat()
                .statusCode(SC_OK)
                .and()
                .assertThat()
                .body("accessToken", notNullValue());
    }

    @Test
    @DisplayName("Создание пользователя с валидными данными")
    public void userCantBeCreatedWithSameCredentials() {
        TestUser user = TestUserGenerator.getRandom();

        ValidatableResponse createResponse = userClient.create(user);
        accessToken = createResponse.extract().path("accessToken");
        userClient.create(user)
                .assertThat()
                .statusCode(SC_FORBIDDEN)
                .and()
                .assertThat()
                .body("success", is(false))
                .assertThat()
                .body("message", is("User already exists"));
    }

    @Test
    @DisplayName("Создание пользователя без почты")
    public void userCantBeCreatedWithoutEmail() {
        TestUser user = TestUserGenerator.getRandom();
        user.setEmail("");

        userClient.create(user)
                .assertThat()
                .statusCode(SC_FORBIDDEN)
                .and()
                .assertThat()
                .body("success", is(false))
                .assertThat()
                .body("message", is("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без пароля")
    public void userCantBeCreatedWithoutPassword() {
        TestUser user = TestUserGenerator.getRandom();
        user.setPassword("");

        userClient.create(user)
                .assertThat()
                .statusCode(SC_FORBIDDEN)
                .and()
                .assertThat()
                .body("success", is(false))
                .assertThat()
                .body("message", is("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без имени")
    public void userCantBeCreatedWithoutName() {
        TestUser user = TestUserGenerator.getRandom();
        user.setName("");

        userClient.create(user)
                .assertThat()
                .statusCode(SC_FORBIDDEN)
                .and()
                .assertThat()
                .body("success", is(false))
                .assertThat()
                .body("message", is("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Изменение почты пользователя")
    public void userEmailChangedSuccessfully() {
        TestUser user = TestUserGenerator.getRandom();

        ValidatableResponse createResponse = userClient.create(user);
        accessToken = createResponse.extract().path("accessToken");

        String newEmail = user.getEmail() + "n";
        TestUserUpdateEmail userUpdateEmail = new TestUserUpdateEmail(newEmail);

        userClient.updateEmailAuthorized(accessToken,userUpdateEmail)
                .assertThat()
                .statusCode(SC_OK)
                .and()
                .assertThat()
                .body("success", is(true))
                .body("user.email", is(newEmail.toLowerCase()));
    }

    @Test
    @DisplayName("Изменение имени пользователя")
    public void userNameChangedSuccessfully() {
        TestUser user = TestUserGenerator.getRandom();

        ValidatableResponse createResponse = userClient.create(user);
        accessToken = createResponse.extract().path("accessToken");

        String newName = user.getName() + "New";
        TestUserUpdateName userUpdateName = new TestUserUpdateName(newName);

        userClient.updateNameAuthorized(accessToken, userUpdateName)
                .assertThat()
                .statusCode(SC_OK)
                .and()
                .assertThat()
                .body("success", is(true))
                .body("user.name", is(newName));
    }

    @Test
    @DisplayName("Изменение почты пользователя без авторизации")
    public void userEmailChangeWithoutAuthorizationFailed() {
        TestUser user = TestUserGenerator.getRandom();

        ValidatableResponse createResponse = userClient.create(user);
        accessToken = createResponse.extract().path("accessToken");

        String newEmail = user.getEmail() + "n";
        TestUserUpdateEmail userUpdateEmail = new TestUserUpdateEmail(newEmail);

        userClient.updateEmailUnauthorized(userUpdateEmail)
                .assertThat()
                .statusCode(SC_UNAUTHORIZED)
                .and()
                .assertThat()
                .body("success", is(false))
                .body("message", is("You should be authorised"));
    }

    @Test
    @DisplayName("Изменение имени пользователя без авторизации")
    public void userNameChangeWithoutAuthorizationFailed() {
        TestUser user = TestUserGenerator.getRandom();

        ValidatableResponse createResponse = userClient.create(user);
        accessToken = createResponse.extract().path("accessToken");

        String newName = user.getName() + "New";
        TestUserUpdateName userUpdateName = new TestUserUpdateName(newName);

        userClient.updateNameUnauthorized(userUpdateName)
                .assertThat()
                .statusCode(SC_UNAUTHORIZED)
                .and()
                .assertThat()
                .body("success", is(false))
                .body("message", is("You should be authorised"));
    }

    @Test
    @DisplayName("Изменение имени пользователя")
    public void userEmailChangeToUsedFailed() {
        TestUser user = TestUserGenerator.getRandom();
        TestUser user2 = TestUserGenerator.getRandom();

        ValidatableResponse createResponse = userClient.create(user);
        accessToken = createResponse.extract().path("accessToken");
        ValidatableResponse createResponse2 = userClient.create(user2);
        String accessToken2 = createResponse2.extract().path("accessToken");
        TestUserUpdateEmail userUpdateEmail = new TestUserUpdateEmail(user2.getEmail());

        ValidatableResponse emailUpdateResponce = userClient.updateEmailAuthorized(accessToken, userUpdateEmail);
        userClient.delete(accessToken2);

        emailUpdateResponce
                .assertThat()
                .statusCode(SC_FORBIDDEN)
                .and()
                .assertThat()
                .body("success", is(false))
                .body("message", is("User with such email already exists"));
    }
}