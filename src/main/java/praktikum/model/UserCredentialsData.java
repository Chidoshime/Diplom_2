package praktikum.model;

public class UserCredentialsData {
    private String email;
    private String password;

    public UserCredentialsData(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public static UserCredentialsData from(User user) {
        return new UserCredentialsData(user.getEmail(), user.getPassword());
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