package io.github.rosestack.notification.domain.value;

import java.time.LocalTime;
import lombok.Data;

/** 时间窗口值对象 */
@Data
public class TimeWindow {
    private LocalTime start;
    private LocalTime end;

    public TimeWindow(LocalTime start, LocalTime end) {
        this.start = start;
        this.end = end;
    }

    public TimeWindow() {
        this.start = LocalTime.of(8, 0);
        this.end = LocalTime.of(18, 0);
    }

    public boolean isWithin(LocalTime time) {
        return time.isAfter(start) && time.isBefore(end);
    }
}
