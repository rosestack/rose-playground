package io.github.rose.user.service;

import io.github.rose.user.dto.UserLoginDTO;
import io.github.rose.user.dto.UserRegisterDTO;
import io.github.rose.user.vo.LoginVO;
import io.github.rose.user.vo.RegisterVO;

/**
 * 认证服务接口
 */
public interface AuthService {
    RegisterVO register(UserRegisterDTO dto);
    LoginVO login(UserLoginDTO dto);
    void logout(Long userId);
}
