package com.company.usermodulith.user;

import com.company.usermodulith.user.internal.UserEntity;
import com.company.usermodulith.user.internal.UserMapper;
import com.company.usermodulith.user.internal.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户服务集成测试 - 使用 Testcontainers MySQL
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Transactional
class UserServiceIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("user_modulith_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Test
    @DisplayName("测试用户服务基本功能")
    void testUserServiceBasicOperations() {
        // 验证测试数据已加载
        List<UserEntity> users = userMapper.selectList(null);
        assertFalse(users.isEmpty(), "应该有测试数据");
        
        System.out.println("找到 " + users.size() + " 个用户");
        users.forEach(user -> 
            System.out.println("用户: " + user.getUsername() + " - " + user.getEmail())
        );
    }

    @Test
    @DisplayName("测试创建用户")
    void testCreateUser() {
        // 创建用户请求
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("testuser");
        request.setEmail("testuser@example.com");
        request.setPhone("13900139000");
        request.setPassword("password123");

        // 创建用户
        UserResponse response = userService.createUser(request);

        // 验证结果
        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals("testuser@example.com", response.getEmail());
        assertEquals("13900139000", response.getPhone());
        assertEquals(UserStatus.ACTIVE, response.getStatus());

        System.out.println("创建用户成功: " + response);
    }

    @Test
    @DisplayName("测试查询用户")
    void testQueryUsers() {
        // 查询所有用户
        List<UserResponse> users = userService.getAllUsers();

        // 验证结果
        assertNotNull(users);
        assertFalse(users.isEmpty());

        System.out.println("查询到 " + users.size() + " 个用户");
        users.forEach(user ->
            System.out.println("用户: " + user.getUsername() + " - " + user.getStatus())
        );
    }

    @Test
    @DisplayName("测试按状态查询用户")
    void testQueryUsersByStatus() {
        // 查询活跃用户
        UserQuery query = new UserQuery();
        query.setStatus("ACTIVE");

        // 使用分页查询
        io.github.rosestack.core.model.PageRequest pageRequest =
            new io.github.rosestack.core.model.PageRequest(1, 10);
        io.github.rosestack.core.model.PageResponse<UserResponse> pageResponse =
            userService.pageUsers(pageRequest, query);

        // 验证结果
        assertNotNull(pageResponse);
        assertNotNull(pageResponse.getRecords());
        assertTrue(pageResponse.getRecords().stream()
            .allMatch(user -> "ACTIVE".equals(user.getStatus().name())));

        System.out.println("活跃用户数量: " + pageResponse.getRecords().size());
    }

    @Test
    @DisplayName("测试更新用户")
    void testUpdateUser() {
        // 先查询一个用户
        List<UserResponse> users = userService.getAllUsers();
        assertFalse(users.isEmpty());

        UserResponse existingUser = users.get(0);
        Long userId = existingUser.getId();

        // 更新用户
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setEmail("updated@example.com");
        updateRequest.setPhone("13900139999");

        UserResponse updatedUser = userService.updateUser(userId, updateRequest);

        // 验证结果
        assertNotNull(updatedUser);
        assertEquals(userId, updatedUser.getId());
        assertEquals("updated@example.com", updatedUser.getEmail());
        assertEquals("13900139999", updatedUser.getPhone());

        System.out.println("更新用户成功: " + updatedUser);
    }

    @Test
    @DisplayName("测试数据库连接信息")
    void testDatabaseConnection() {
        System.out.println("=== MySQL Testcontainer 信息 ===");
        System.out.println("JDBC URL: " + mysql.getJdbcUrl());
        System.out.println("用户名: " + mysql.getUsername());
        System.out.println("数据库名: " + mysql.getDatabaseName());
        System.out.println("容器ID: " + mysql.getContainerId());
        System.out.println("映射端口: " + mysql.getMappedPort(3306));
        
        assertTrue(mysql.isRunning(), "MySQL 容器应该正在运行");
    }
}
