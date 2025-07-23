package io.github.rose.user.dto;

import io.github.rose.user.entity.UserStatus;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 用户分页查询请求对象
 *
 * @author Chen Soul
 * @since 1.0.0
 */
@Data
public class UserPageRequest {
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户状态
     */
    private UserStatus status;
    
    /**
     * 开始日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    /**
     * 结束日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}