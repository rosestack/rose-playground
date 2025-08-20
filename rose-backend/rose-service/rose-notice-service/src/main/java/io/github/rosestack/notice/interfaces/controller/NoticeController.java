package io.github.rosestack.notice.interfaces.controller;

import io.github.rosestack.notice.application.command.SendNoticeCommand;
import io.github.rosestack.notice.application.service.NoticeApplicationService;
import io.github.rosestack.notice.interfaces.assembler.NoticeAssembler;
import io.github.rosestack.notice.interfaces.dto.NoticeDTO;
import io.github.rosestack.notice.interfaces.dto.SendNoticeRequest;
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
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    /**
     * 通知应用服务
     */
    private final NoticeApplicationService noticeApplicationService;

    /**
     * 通知装配器
     */
    private final NoticeAssembler noticeAssembler;

    /**
     * 发送通知
     *
     * @param request 发送通知请求
     * @return 响应结果
     */
    @PostMapping("/send")
    public ResponseEntity<Void> sendNotice(@Valid @RequestBody SendNoticeRequest request) {
        SendNoticeCommand command = noticeAssembler.toCommand(request);
        noticeApplicationService.sendNotice(command);
        return ResponseEntity.ok().build();
    }

    /**
     * 根据ID查询通知
     *
     * @param id 通知ID
     * @return 通知信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<NoticeDTO> getNotice(@PathVariable String id) {
        // TODO: 实现查询逻辑
        return ResponseEntity.ok().build();
    }
}
