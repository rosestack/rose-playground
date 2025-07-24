package io.github.rose.core.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * 金额值对象（不可变）
 *
 * @author rose
 */
@Getter
@EqualsAndHashCode
@ToString
public final class Money {
    private final BigDecimal amount;
    private final Currency currency;
    
    @JsonCreator
    public Money(@JsonProperty("amount") BigDecimal amount, 
                 @JsonProperty("currency") Currency currency) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Currency cannot be null");
        }
        this.amount = amount;
        this.currency = currency;
    }
    
    /**
     * 创建人民币金额
     */
    public static Money cny(BigDecimal amount) {
        return new Money(amount, Currency.getInstance("CNY"));
    }
    
    /**
     * 创建美元金额
     */
    public static Money usd(BigDecimal amount) {
        return new Money(amount, Currency.getInstance("USD"));
    }
    
    /**
     * 加法
     */
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add different currencies");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }
    
    /**
     * 减法
     */
    public Money subtract(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot subtract different currencies");
        }
        return new Money(this.amount.subtract(other.amount), this.currency);
    }
    
    /**
     * 乘法
     */
    public Money multiply(BigDecimal multiplier) {
        return new Money(this.amount.multiply(multiplier), this.currency);
    }
    
    /**
     * 是否为正数
     */
    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * 是否为零
     */
    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }
}
