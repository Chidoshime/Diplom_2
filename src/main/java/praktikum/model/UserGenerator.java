package praktikum.model;

import com.github.javafaker.Faker;

public class UserGenerator {

    public static User getRandom() {
        Faker faker = new Faker();
        String email = faker.internet().emailAddress();
        String password = faker.internet().password(6,10);
        String name = faker.harryPotter().character();
        return new User(email, password, name);
    }
}