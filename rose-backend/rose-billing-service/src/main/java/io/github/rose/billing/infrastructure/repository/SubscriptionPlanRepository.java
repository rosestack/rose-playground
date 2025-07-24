package io.github.rose.billing.infrastructure.repository;

import io.github.rose.billing.domain.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 订阅计划数据访问接口
 *
 * @author rose
 */
@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, String> {

    /**
     * 根据代码查找计划
     */
    Optional<SubscriptionPlan> findByCode(String code);

    /**
     * 查找启用的计划
     */
    List<SubscriptionPlan> findByEnabledTrueOrderByBasePrice();

    /**
     * 根据租户查找计划
     */
    List<SubscriptionPlan> findByTenantIdOrTenantIdIsNull(String tenantId);

    /**
     * 查找有效期内的计划
     */
    @Query("SELECT p FROM SubscriptionPlan p WHERE p.enabled = true " +
           "AND (p.effectiveDate IS NULL OR p.effectiveDate <= :now) " +
           "AND (p.expiryDate IS NULL OR p.expiryDate > :now)")
    List<SubscriptionPlan> findValidPlans(LocalDateTime now);
}
