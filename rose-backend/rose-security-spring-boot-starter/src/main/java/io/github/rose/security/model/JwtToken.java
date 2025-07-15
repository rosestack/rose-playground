
package io.github.rose.security.model;

import java.io.Serializable;

public interface JwtToken extends Serializable {
    String getToken();
}
