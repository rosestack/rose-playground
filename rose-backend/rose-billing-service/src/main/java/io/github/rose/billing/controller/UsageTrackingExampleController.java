package io.github.rose.billing.controller;

import io.github.rose.billing.aspect.annotation.*;
import io.github.rose.interfaces.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 使用量监控示例控制器
 * 演示如何使用AOP注解自动监控使用量
 *
 * @author rose
 */
@RestController
@RequestMapping("/api/example")
@RequiredArgsConstructor
public class UsageTrackingExampleController {

    /**
     * API调用监控示例
     */
    @GetMapping("/users")
    @TrackApiUsage("/api/example/users")  // 自动记录API调用次数
    public ApiResponse<List<String>> getUsers() {
        // 模拟获取用户列表
        return ApiResponse.success(List.of("user1", "user2", "user3"));
    }

    /**
     * 文件上传监控示例
     */
    @PostMapping("/upload")
    @TrackStorageUsage(resourceType = "DOCUMENT")  // 自动记录存储使用量
    public ApiResponse<Long> uploadFile(@RequestParam("file") Object file) {
        // 模拟文件上传，返回文件大小（字节）
        long fileSize = 1024 * 1024; // 1MB
        return ApiResponse.success(fileSize);
    }

    /**
     * 用户创建监控示例
     */
    @PostMapping("/users")
    @TrackUserChange(operation = "CREATE")  // 自动记录用户数变化
    public ApiResponse<String> createUser(@RequestBody Object userRequest) {
        // 模拟创建用户
        return ApiResponse.success("用户创建成功");
    }

    /**
     * 邮件发送监控示例
     */
    @PostMapping("/send-email")
    @TrackEmailUsage(emailType = "MARKETING")  // 自动记录邮件发送次数
    public ApiResponse<String> sendEmail(@RequestBody Object emailRequest) {
        // 模拟发送邮件
        return ApiResponse.success("邮件发送成功");
    }

    /**
     * 短信发送监控示例
     */
    @PostMapping("/send-sms")
    @TrackSmsUsage(smsType = "VERIFICATION")  // 自动记录短信发送次数
    public ApiResponse<String> sendSms(@RequestBody Object smsRequest) {
        // 模拟发送短信
        return ApiResponse.success("短信发送成功");
    }
}
