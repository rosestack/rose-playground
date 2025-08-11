package io.github.rosestack.iam.vo;

public class LoginVO {
    private String token;
    private Long userId;
    private String username;
    // getter/setter
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
