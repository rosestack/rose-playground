package io.github.rosestack.notice.domain.event.listener;

import io.github.rosestack.notice.domain.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NoticeEventListener {
    private final NoticeRepository noticeRepository;
}
