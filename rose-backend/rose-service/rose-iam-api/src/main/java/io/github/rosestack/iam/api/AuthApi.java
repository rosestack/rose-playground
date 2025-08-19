package io.github.rosestack.iam.api;

import io.github.rosestack.core.model.ApiResponse;
import io.github.rosestack.iam.dto.UserLoginDTO;
import io.github.rosestack.iam.dto.UserRegisterDTO;
import io.github.rosestack.iam.vo.LoginVO;
import io.github.rosestack.iam.vo.RegisterVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface AuthApi {
	@PostMapping("/api/auth/register")
	ApiResponse<RegisterVO> register(@RequestBody UserRegisterDTO dto);

	@PostMapping("/api/auth/login")
	ApiResponse<LoginVO> login(@RequestBody UserLoginDTO dto);

	@PostMapping("/api/auth/logout")
	ApiResponse<Void> logout();
}
