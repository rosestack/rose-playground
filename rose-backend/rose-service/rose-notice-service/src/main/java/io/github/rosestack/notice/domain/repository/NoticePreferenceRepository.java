package io.github.rosestack.notice.domain.repository;

import io.github.rosestack.notice.domain.entity.NoticePreference;
import java.util.Optional;

public interface NoticePreferenceRepository {
    Optional<NoticePreference> findById(String id);

    void save(NoticePreference preference);

    void update(NoticePreference preference);

    void delete(String id);
}
