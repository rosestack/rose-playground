package io.github.rose.notice;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.Map;

@Data
@ToString
@Builder
public class SenderConfiguration {
    private String templateType;
    private String channelType;
    private Map<String, Object> config;
}
