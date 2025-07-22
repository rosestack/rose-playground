package io.github.rose.notice;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SenderConfiguration {
    private String templateType;
    private String channelType;
    private JsonNode config;
}
