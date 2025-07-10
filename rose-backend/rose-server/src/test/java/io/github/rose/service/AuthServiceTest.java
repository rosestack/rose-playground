package io.github.rose.service;

import io.github.rose.common.exception.BusinessException;
import io.github.rose.user.dto.UserLoginDTO;
import io.github.rose.user.dto.UserRegisterDTO;
import io.github.rose.user.repository.UserRepository;
import io.github.rose.user.service.AuthService;
import io.github.rose.user.vo.LoginVO;
import io.github.rose.user.vo.RegisterVO;
import io.github.rose.server.RoseServerApplication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(classes = RoseServerApplication.class)
@DirtiesContext
public class AuthServiceTest {
    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();

        tesRegister();
    }

    void tesRegister() {
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setUsername("testuser");
        userRegisterDTO.setPassword("testpass");
        userRegisterDTO.setEmail("test@example.com");
        RegisterVO vo = authService.register(userRegisterDTO);
        Assertions.assertNotNull(vo.getEmail());
        Assertions.assertEquals("testuser", vo.getUsername());
    }

    @Test
    void testLoginSuccess() {
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("testpass");
        loginDTO.setCaptcha("mock");
        LoginVO vo = authService.login(loginDTO);
        Assertions.assertNotNull(vo.getToken());
        Assertions.assertEquals("testuser", vo.getUsername());
    }

    @Test
    void testLoginFail() {
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("wrongpass");
        loginDTO.setCaptcha("mock");
        Assertions.assertThrows(BusinessException.class, () -> authService.login(loginDTO));
    }
}
