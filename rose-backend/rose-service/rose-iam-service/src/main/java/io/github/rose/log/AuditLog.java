package io.github.rose.log;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class AuditLog extends LoginLog implements Serializable {
    private static final long serialVersionUID = 1129753896999673095L;

    private String name;

    private EntityType entityType;
    private String entityId;
    private String entityName;

    //requestParam,requestBody
    private JsonNode actionData;
    private ActionType actionType;
    private ActionStatus actionStatus;
    private String actionFailure;

}
