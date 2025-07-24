package io.github.rose.billing.infrastructure.repository;

import io.github.rose.billing.domain.entity.TenantSubscription;
import io.github.rose.billing.domain.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 租户订阅数据访问接口
 *
 * @author rose
 */
@Repository
public interface TenantSubscriptionRepository extends JpaRepository<TenantSubscription, String> {

    /**
     * 根据租户ID查找当前订阅
     */
    Optional<TenantSubscription> findByTenantId(String tenantId);

    /**
     * 查找活跃订阅
     */
    Optional<TenantSubscription> findByTenantIdAndStatus(String tenantId, SubscriptionStatus status);

    /**
     * 根据租户ID查找活跃订阅
     */
    @Query("SELECT s FROM TenantSubscription s WHERE s.tenantId = :tenantId " +
           "AND s.status IN ('ACTIVE', 'TRIAL')")
    Optional<TenantSubscription> findActiveByTenantId(String tenantId);

    /**
     * 查找需要计费的订阅
     */
    List<TenantSubscription> findByNextBillingDateBeforeAndStatusIn(
        LocalDateTime date, List<SubscriptionStatus> statuses);

    /**
     * 查找试用期即将到期的订阅
     */
    @Query("SELECT s FROM TenantSubscription s WHERE s.inTrial = true " +
           "AND s.trialEndDate BETWEEN :startDate AND :endDate")
    List<TenantSubscription> findTrialExpiringSoon(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 统计租户订阅数
     */
    long countByStatus(SubscriptionStatus status);
}
