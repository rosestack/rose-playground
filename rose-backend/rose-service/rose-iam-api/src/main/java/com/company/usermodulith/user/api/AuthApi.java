package com.company.usermodulith.user.api;

import com.company.usermodulith.user.dto.UserLoginDTO;
import com.company.usermodulith.user.dto.UserRegisterDTO;
import com.company.usermodulith.user.vo.LoginVO;
import com.company.usermodulith.user.vo.RegisterVO;
import io.github.rosestack.interfaces.dto.ApiResponse;
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
