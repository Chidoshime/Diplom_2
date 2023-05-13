package praktikum.model;

public class TestUserCredentialsData {
    private String email;
    private String password;

    public TestUserCredentialsData(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public static TestUserCredentialsData from(TestUser user) {
        return new TestUserCredentialsData(user.getEmail(), user.getPassword());
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}