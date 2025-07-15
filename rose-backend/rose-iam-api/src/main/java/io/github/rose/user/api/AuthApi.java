package io.github.rose.user.api;

import io.github.rose.common.util.Result;
import io.github.rose.user.dto.UserLoginDTO;
import io.github.rose.user.dto.UserRegisterDTO;
import io.github.rose.user.vo.LoginVO;
import io.github.rose.user.vo.RegisterVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface AuthApi {
    @PostMapping("/api/auth/register")
    Result<RegisterVO> register(@RequestBody UserRegisterDTO dto);

    @PostMapping("/api/auth/login")
    Result<LoginVO> login(@RequestBody UserLoginDTO dto);

    @PostMapping("/api/auth/logout")
    Result<Void> logout();
}
