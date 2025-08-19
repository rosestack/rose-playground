package com.company.todo.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Application-level configurable properties bound from application.yaml.
 * Keep business parameters configurable instead of hardcoding in code.
 */
@Validated
@ConfigurationProperties(prefix = "app.mybatis")
public class MybatisProperties {

    @NotNull private final Pagination pagination = new Pagination();

    public Pagination getPagination() {
        return pagination;
    }

    public static class Pagination {
        /**
         * Maximum allowed page size for MyBatis-Plus PaginationInnerInterceptor.
         */
        @Min(1) @Max(10000) private long maxPageSize = 100L;

        public long getMaxPageSize() {
            return maxPageSize;
        }

        public void setMaxPageSize(long maxPageSize) {
            this.maxPageSize = maxPageSize;
        }
    }
}
