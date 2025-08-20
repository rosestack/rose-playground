package io.github.rosestack.notify;

import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Builder
public class SenderConfiguration {
    private String templateType;
    private String channelType;
    private Map<String, Object> config;

    public String getTemplateType() {
        return templateType;
    }

    public String getChannelType() {
        return channelType;
    }

    public Map<String, Object> getConfig() {
        return config;
    }
}
