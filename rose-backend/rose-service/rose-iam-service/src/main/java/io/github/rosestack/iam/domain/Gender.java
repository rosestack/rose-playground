package io.github.rosestack.iam.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Gender {
    M("M"),
    F("F"),
    U("U"),
    ;
    private String value;
}