package com.company.usermodulith.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户更新请求
 *
 * @author Chen Soul
 * @since 1.0.0
 */
@Data
public class UserUpdateRequest {

	@Size(min = 3, max = 20, message = "用户名长度必须在3-20之间")
	@Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
	private String username;

	@Email(message = "邮箱格式不正确")
	@Size(max = 100, message = "邮箱长度不能超过100字符")
	private String email;

	@Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
	private String phone;
}
