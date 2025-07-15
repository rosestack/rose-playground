
package io.github.rose.security.model;

import lombok.Data;

@Data
public class LoginResponse {

    private String token;

    private String refreshToken;

}
