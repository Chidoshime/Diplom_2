package praktikum.model;

import com.github.javafaker.Faker;

public class TestUserGenerator {

    public static TestUser getRandom() {
        Faker faker = new Faker();
        String email = faker.internet().emailAddress();
        String password = faker.internet().password(6,10);
        String name = faker.harryPotter().character();
        return new TestUser(email, password, name);
    }
}