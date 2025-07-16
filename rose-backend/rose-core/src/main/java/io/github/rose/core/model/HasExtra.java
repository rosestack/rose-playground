package io.github.rose.core.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface HasExtra {
    ObjectMapper mapper = new ObjectMapper();
    Logger log = LoggerFactory.getLogger(HasExtra.class);

    JsonNode getExtra();

    void setExtra(JsonNode extra);

    default JsonNode getJson(Supplier<JsonNode> jsonData, Supplier<byte[]> binaryData) {
        JsonNode json = jsonData.get();
        if (json != null) {
            return json;
        } else {
            byte[] data = binaryData.get();
            if (data != null) {
                try {
                    return mapper.readTree(new ByteArrayInputStream(data));
                } catch (IOException e) {
                    log.warn("Can't deserialize jackson data: ", e);
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    default void setJson(JsonNode json, Consumer<JsonNode> jsonConsumer, Consumer<byte[]> bytesConsumer) {
        jsonConsumer.accept(json);
        try {
            bytesConsumer.accept(mapper.writeValueAsBytes(json));
        } catch (JsonProcessingException e) {
            log.warn("Can't serialize jackson data: ", e);
        }
    }

    default void setExtraField(String field, JsonNode value) {
        JsonNode additionalInfo = getExtra();
        if (!(additionalInfo instanceof ObjectNode)) {
            additionalInfo = mapper.createObjectNode();
        }
        ((ObjectNode) additionalInfo).set(field, value);
        setExtra(additionalInfo);
    }

    default <T> T getExtraField(String field, Function<JsonNode, T> mapper, T defaultValue) {
        JsonNode additionalInfo = getExtra();
        if (additionalInfo != null && additionalInfo.has(field)) {
            return mapper.apply(additionalInfo.get(field));
        }
        return defaultValue;
    }
}
