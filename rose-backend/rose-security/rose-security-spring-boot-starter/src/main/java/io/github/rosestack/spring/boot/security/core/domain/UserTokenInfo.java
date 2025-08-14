package io.github.rosestack.spring.boot.security.core.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserTokenInfo {

    private TokenInfo tokenInfo;

    private String username;
}
