package io.github.rosestack.notification.interfaces.controller;

import io.github.rosestack.notification.application.command.SendNotificationCommand;
import io.github.rosestack.notification.application.service.NotificationApplicationService;
import io.github.rosestack.notification.interfaces.assembler.NotificationAssembler;
import io.github.rosestack.notification.interfaces.dto.NotificationDTO;
import io.github.rosestack.notification.interfaces.dto.SendNotificationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 通知控制器
 *
 * <p>提供通知相关的 REST API 接口。
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    /** 通知应用服务 */
    private final NotificationApplicationService notificationApplicationService;

    /** 通知装配器 */
    private final NotificationAssembler notificationAssembler;

    /**
     * 发送通知
     *
     * @param request 发送通知请求
     * @return 响应结果
     */
    @PostMapping("/send")
    public ResponseEntity<Void> sendNotification(@Valid @RequestBody SendNotificationRequest request) {
        SendNotificationCommand command = notificationAssembler.toCommand(request);
        notificationApplicationService.sendNotification(command);
        return ResponseEntity.ok().build();
    }

    /**
     * 根据ID查询通知
     *
     * @param id 通知ID
     * @return 通知信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<NotificationDTO> getNotification(@PathVariable String id) {
        // TODO: 实现查询逻辑
        return ResponseEntity.ok().build();
    }
}
