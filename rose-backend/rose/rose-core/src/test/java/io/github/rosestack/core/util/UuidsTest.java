package io.github.rosestack.core.util;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class UuidsTest {
    private static final Logger log = LoggerFactory.getLogger(UuidsTest.class);

    @Test
    void testGetUUID() {
        UUID uuid = Uuids.getUUID();
        log.info("uuid: {}", uuid);
        // Encode for URL
        String urlSafeId = Uuids.uuidToBase64(uuid);
        log.info("URL Safe ID:   {}", urlSafeId); // e.g., AZZK6H62dLudJ60SgK7tTQ

        // Decode from URL
        UUID decodedUuid = Uuids.base64ToUuid(urlSafeId);
        log.info("Decoded UUID:  {}", decodedUuid);

        log.info("Match: {}", uuid.equals(decodedUuid));
        Assertions.assertEquals(uuid, decodedUuid);
    }

    @Test
    void testUuidInMap() {
        Map<String, Object> map = new HashMap<>();
        UUID uuid = Uuids.getUUID();
        map.put("key", uuid);
        log.info("Map: " + map);
        String json = JsonUtils.toString(map);
        log.info("Json: " + json);
        Map<String, Object> map2 = JsonUtils.fromString(json, new TypeReference<>() {});
        log.info("Map2: " + map2);
    }
}
