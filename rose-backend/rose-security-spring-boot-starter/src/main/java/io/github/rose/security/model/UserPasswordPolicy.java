
package io.github.rose.security.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserPasswordPolicy implements Serializable {

    private Integer minimumLength;
    private Integer maximumLength;
    private Integer minimumUppercaseLetters;
    private Integer minimumLowercaseLetters;
    private Integer minimumDigits;
    private Integer minimumSpecialCharacters;
    private Boolean allowWhitespaces = true;
    private Boolean forceUserToResetPasswordIfNotValid = false;
    private Integer passwordExpirationPeriodDays;
    private Integer passwordReuseFrequencyDays;

}
