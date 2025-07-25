package io.github.rose.billing.repository;

import io.github.rose.billing.entity.BaseTenantSubscription;
import io.github.rose.billing.enums.SubscriptionStatus;
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
public interface TenantSubscriptionRepository extends JpaRepository<BaseTenantSubscription, String> {

    /**
     * 根据租户ID查找当前订阅
     */
    Optional<BaseTenantSubscription> findByTenantId(String tenantId);

    /**
     * 查找活跃订阅
     */
    Optional<BaseTenantSubscription> findByTenantIdAndStatus(String tenantId, SubscriptionStatus status);

    /**
     * 根据租户ID查找活跃订阅
     */
    @Query("SELECT s FROM BaseTenantSubscription s WHERE s.tenantId = :tenantId " +
           "AND s.status IN ('ACTIVE', 'TRIAL')")
    Optional<BaseTenantSubscription> findActiveByTenantId(String tenantId);

    /**
     * 查找需要计费的订阅
     */
    List<BaseTenantSubscription> findByNextBillingDateBeforeAndStatusIn(
        LocalDateTime date, List<SubscriptionStatus> statuses);

    /**
     * 查找试用期即将到期的订阅
     */
    @Query("SELECT s FROM BaseTenantSubscription s WHERE s.inTrial = true " +
           "AND s.trialEndDate BETWEEN :startDate AND :endDate")
    List<BaseTenantSubscription> findTrialExpiringSoon(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 统计租户订阅数
     */
    long countByStatus(SubscriptionStatus status);
}
