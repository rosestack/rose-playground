package io.github.rose.security.rbac.annotation.enhanced;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 基于时间的权限注解
 * <p>
 * 支持基于时间条件的权限控制，可以设置权限的有效时间范围、
 * 工作时间限制、节假日限制等。
 * </p>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * @TimeBasedPermission(
 *     permission = "system:backup",
 *     allowedHours = {"02:00-04:00", "22:00-23:59"},
 *     allowedDays = {DayOfWeek.SATURDAY, DayOfWeek.SUNDAY},
 *     timezone = "Asia/Shanghai"
 * )
 * public void performBackup() {
 *     // 备份操作只能在指定时间执行
 * }
 * }</pre>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TimeBasedPermission {

    /**
     * 权限名称
     *
     * @return 权限名称
     */
    String permission();

    /**
     * 允许的时间范围（24小时制）
     * <p>
     * 格式：HH:mm-HH:mm
     * 例如：{"09:00-18:00", "20:00-22:00"}
     * </p>
     *
     * @return 允许的时间范围数组
     */
    String[] allowedHours() default {};

    /**
     * 允许的星期几
     *
     * @return 允许的星期几数组
     */
    DayOfWeek[] allowedDays() default {};

    /**
     * 禁止的日期（格式：yyyy-MM-dd）
     *
     * @return 禁止的日期数组
     */
    String[] forbiddenDates() default {};

    /**
     * 时区
     *
     * @return 时区ID
     */
    String timezone() default "UTC";

    /**
     * 权限描述
     *
     * @return 权限描述
     */
    String description() default "";

    /**
     * 时间条件不满足时的错误消息
     *
     * @return 错误消息
     */
    String timeErrorMessage() default "当前时间不允许执行此操作";

    /**
     * 是否必须拥有权限
     *
     * @return 是否必须拥有权限
     */
    boolean required() default true;

    /**
     * 权限检查失败时的错误消息
     *
     * @return 错误消息
     */
    String errorMessage() default "权限不足";

    /**
     * 星期几枚举
     */
    enum DayOfWeek {
        MONDAY(1),
        TUESDAY(2),
        WEDNESDAY(3),
        THURSDAY(4),
        FRIDAY(5),
        SATURDAY(6),
        SUNDAY(7);

        private final int value;

        DayOfWeek(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static DayOfWeek of(int dayOfWeek) {
            for (DayOfWeek day : values()) {
                if (day.value == dayOfWeek) {
                    return day;
                }
            }
            throw new IllegalArgumentException("Invalid day of week: " + dayOfWeek);
        }
    }
}