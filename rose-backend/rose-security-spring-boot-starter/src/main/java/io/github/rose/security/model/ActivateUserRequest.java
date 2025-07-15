
package io.github.rose.security.model;

import lombok.Data;

@Data
public class ActivateUserRequest {

    private String activateToken;
    private String password;
}
