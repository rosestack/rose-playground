
package io.github.rose.security.auth.exception;

import org.springframework.security.authentication.AccountStatusException;

public class UserPasswordNotValidException extends AccountStatusException {

    public UserPasswordNotValidException(String msg) {
        super(msg);
    }

}
