package model.register;

public record RegisterRequest(String username, String password, String email) {
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public String getEmail() {
        return email;
    }
}
