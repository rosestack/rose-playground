
package io.github.rose.security.model;

import lombok.Data;

@Data
public class ResetPasswordRequest {

    private String resetToken;
    private String password;
}
