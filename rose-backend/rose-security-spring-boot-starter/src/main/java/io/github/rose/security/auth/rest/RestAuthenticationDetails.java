
package io.github.rose.security.auth.rest;

import io.github.rose.core.util.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import ua_parser.Client;
import ua_parser.Parser;

import java.io.Serializable;

@Data
public class RestAuthenticationDetails implements Serializable {
    private final String clientIp;
    private final Client userAgent;

    public RestAuthenticationDetails(HttpServletRequest request) {
        this.clientIp = ServletUtils.getClientIp(request);
        this.userAgent = getUserAgent(request);
    }

    private Client getUserAgent(HttpServletRequest request) {
        Parser uaParser = new Parser();
        return uaParser.parse(request.getHeader("User-Agent"));
    }
}
