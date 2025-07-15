package io.github.rose.log;

import lombok.Getter;

@Getter
public enum ActionType {
    CREATE("create"),

    ADD("add"),

    REMOVE("remove"),

    QUERY("query"),

    DELETE("delete"),

    IMPORT("import"),

    EXPORT("export"),

    UPDATE("update"),

    REFRESH("refresh"),

    SYNC("sync"),

    INVITE("invite"),

    RESIGN("resign"),

    RECOVER("recover"),

    DISABLE("disable"),

    ENABLE("enable"),

    ACTIVATE("activate"),

    DEACTIVATE("deactivate"),
    ;

    private String value;
    private boolean read;

    ActionType(String value) {
        this.value = value;
        this.read = false;
    }

    ActionType(String value, boolean read) {
        this.value = value;
        this.read = read;
    }
}
