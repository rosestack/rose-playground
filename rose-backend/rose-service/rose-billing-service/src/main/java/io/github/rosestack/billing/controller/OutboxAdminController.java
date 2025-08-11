package io.github.rosestack.billing.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.rosestack.billing.entity.OutboxRecord;
import io.github.rosestack.billing.enums.OutboxStatus;
import io.github.rosestack.billing.repository.OutboxRepository;
import io.github.rosestack.billing.service.OutboxService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/outbox")
@RequiredArgsConstructor
@Validated
public class OutboxAdminController {

    private final OutboxRepository outboxRepository;
    private final OutboxService outboxService;

    @GetMapping
    public List<OutboxRecord> list(
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) int size,
            @RequestParam(required = false) String status) {
        LambdaQueryWrapper<OutboxRecord> qw = new LambdaQueryWrapper<>();
        if (status != null) {
            qw.eq(OutboxRecord::getStatus, OutboxStatus.valueOf(status));
        }
        qw.last("limit " + size);
        return outboxRepository.selectList(qw);
    }

    @PostMapping("/{id}/retry")
    public String retry(@PathVariable String id) {
        OutboxRecord rec = outboxRepository.selectById(id);
        if (rec == null) return "NOT_FOUND";
        rec.setStatus(OutboxStatus.PENDING);
        rec.setNextRetryAt(LocalDateTime.now());
        outboxRepository.updateById(rec);
        return "OK";
    }

    @PostMapping("/relay")
    public String relay(@RequestParam(defaultValue = "100") @Min(1) @Max(1000) int limit) {
        int n = outboxService.relayPending(limit);
        return "SENT=" + n;
    }
}

