package io.github.rosestack.billing.application.service;

import io.github.rosestack.billing.domain.enums.FeatureType;
import io.github.rosestack.billing.domain.enums.FeatureStatus;
import io.github.rosestack.billing.domain.enums.ResetPeriod;
import io.github.rosestack.billing.domain.feature.BillFeature;
import io.github.rosestack.billing.domain.feature.BillFeatureMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 计费功能管理服务
 *
 * 提供功能的创建、更新、查询和删除等操作
 * 支持多租户数据隔离和功能状态管理
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BillFeatureService {

    private final BillFeatureMapper billFeatureMapper;

    /**
     * 创建新功能
     */
    @Transactional
    public BillFeature createFeature(BillFeature feature) {
        log.info("创建新功能: {}", feature.getCode());

        // 验证功能数据
        validateFeature(feature);

        // 检查功能代码是否已存在
        if (existsByTenantAndCode(feature.getTenantId(), feature.getCode())) {
            throw new IllegalArgumentException("功能代码已存在: " + feature.getCode());
        }

        // 设置默认状态
        if (feature.getStatus() == null) {
            feature.setStatus(FeatureStatus.ACTIVE);
        }

        // 保存功能
        billFeatureMapper.insert(feature);

        log.info("功能创建成功: id={}, code={}", feature.getId(), feature.getCode());
        return feature;
    }

    /**
     * 更新功能信息
     */
    @Transactional
    public BillFeature updateFeature(BillFeature feature) {
        log.info("更新功能: id={}, code={}", feature.getId(), feature.getCode());

        // 验证功能数据
        validateFeature(feature);

        // 检查功能是否存在
        BillFeature existingFeature = getFeatureById(feature.getId());
        if (existingFeature == null) {
            throw new IllegalArgumentException("功能不存在: id=" + feature.getId());
        }

        // 如果更改了功能代码，检查新代码是否已存在
        if (!existingFeature.getCode().equals(feature.getCode())) {
            if (existsByTenantAndCode(feature.getTenantId(), feature.getCode())) {
                throw new IllegalArgumentException("功能代码已存在: " + feature.getCode());
            }
        }

        // 更新功能
        billFeatureMapper.updateById(feature);

        log.info("功能更新成功: id={}, code={}", feature.getId(), feature.getCode());
        return feature;
    }

    /**
     * 根据ID获取功能
     */
    public BillFeature getFeatureById(Long id) {
        if (id == null) {
            return null;
        }
        return billFeatureMapper.selectById(id);
    }

    /**
     * 根据租户ID和功能代码获取功能
     */
    public BillFeature getFeatureByTenantAndCode(String tenantId, String code) {
        if (tenantId == null || code == null || code.trim().isEmpty()) {
            return null;
        }
        return billFeatureMapper.findByTenantAndCode(tenantId, code);
    }

    /**
     * 获取系统级功能（租户ID="0"）
     */
    public BillFeature getSystemFeatureByCode(String code) {
        return getFeatureByTenantAndCode("0", code);
    }

    /**
     * 根据租户ID获取所有激活功能
     */
    public List<BillFeature> getEnabledFeaturesByTenant(String tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("租户ID不能为空");
        }
        return billFeatureMapper.findEnabledFeaturesByTenant(tenantId);
    }

    /**
     * 获取所有系统级激活功能
     */
    public List<BillFeature> getEnabledSystemFeatures() {
        return billFeatureMapper.findEnabledSystemFeatures();
    }

    /**
     * 根据租户ID和功能类型获取功能列表
     */
    public List<BillFeature> getFeaturesByTenantAndType(String tenantId, FeatureType type) {
        if (tenantId == null || type == null) {
            throw new IllegalArgumentException("租户ID和功能类型不能为空");
        }
        return billFeatureMapper.findByTenantAndType(tenantId, type);
    }

    /**
     * 根据重置周期获取功能列表
     */
    public List<BillFeature> getFeaturesByResetPeriod(ResetPeriod resetPeriod) {
        if (resetPeriod == null) {
            throw new IllegalArgumentException("重置周期不能为空");
        }
        return billFeatureMapper.findByResetPeriod(resetPeriod);
    }

    /**
     * 获取需要重置的功能列表
     */
    public List<BillFeature> getFeaturesNeedingReset() {
        return billFeatureMapper.findFeaturesNeedingReset();
    }

    /**
     * 激活功能
     */
    @Transactional
    public void activateFeature(Long id) {
        log.info("激活功能: id={}", id);

        BillFeature feature = getFeatureById(id);
        if (feature == null) {
            throw new IllegalArgumentException("功能不存在: id=" + id);
        }

        feature.activate();
        billFeatureMapper.updateById(feature);

        log.info("功能激活成功: id={}, code={}", id, feature.getCode());
    }

    /**
     * 禁用功能
     */
    @Transactional
    public void deactivateFeature(Long id) {
        log.info("禁用功能: id={}", id);

        BillFeature feature = getFeatureById(id);
        if (feature == null) {
            throw new IllegalArgumentException("功能不存在: id=" + id);
        }

        feature.deactivate();
        billFeatureMapper.updateById(feature);

        log.info("功能禁用成功: id={}, code={}", id, feature.getCode());
    }

    /**
     * 删除功能
     */
    @Transactional
    public void deleteFeature(Long id) {
        log.info("删除功能: id={}", id);

        BillFeature feature = getFeatureById(id);
        if (feature == null) {
            throw new IllegalArgumentException("功能不存在: id=" + id);
        }

        // 检查功能是否可以删除
        if (!feature.canBeDeleted()) {
            throw new IllegalStateException("功能当前状态不允许删除: " + feature.getStatus());
        }

        // 软删除功能
        billFeatureMapper.deleteById(id);

        log.info("功能删除成功: id={}, code={}", id, feature.getCode());
    }

    /**
     * 检查功能代码是否存在
     */
    public boolean existsByTenantAndCode(String tenantId, String code) {
        if (tenantId == null || code == null || code.trim().isEmpty()) {
            return false;
        }
        return billFeatureMapper.existsByTenantAndCode(tenantId, code);
    }

    /**
     * 验证功能有效性
     */
    public boolean isValidFeature(BillFeature feature) {
        if (feature == null) {
            return false;
        }
        return feature.isValidFeature();
    }

    /**
     * 创建系统级功能
     */
    @Transactional
    public BillFeature createSystemFeature(String code, String name, FeatureType type, String unit) {
        log.info("创建系统级功能: code={}, name={}", code, name);

        BillFeature feature = new BillFeature();
        feature.setTenantId("0"); // 系统级功能租户ID为"0"
        feature.setCode(code);
        feature.setName(name);
        feature.setType(type);
        feature.setUnit(unit);
        feature.setStatus(FeatureStatus.ACTIVE);

        return createFeature(feature);
    }

    /**
     * 复制系统级功能到租户
     */
    @Transactional
    public BillFeature copySystemFeatureToTenant(String tenantId, String systemFeatureCode) {
        log.info("复制系统级功能到租户: tenantId={}, featureCode={}", tenantId, systemFeatureCode);

        // 获取系统级功能
        BillFeature systemFeature = getSystemFeatureByCode(systemFeatureCode);
        if (systemFeature == null) {
            throw new IllegalArgumentException("系统级功能不存在: " + systemFeatureCode);
        }

        // 检查租户是否已有此功能
        if (existsByTenantAndCode(tenantId, systemFeatureCode)) {
            throw new IllegalArgumentException("租户已存在此功能: " + systemFeatureCode);
        }

        // 创建租户功能
        BillFeature tenantFeature = new BillFeature();
        tenantFeature.setTenantId(tenantId);
        tenantFeature.setCode(systemFeature.getCode());
        tenantFeature.setName(systemFeature.getName());
        tenantFeature.setDescription(systemFeature.getDescription());
        tenantFeature.setType(systemFeature.getType());
        tenantFeature.setUnit(systemFeature.getUnit());
        tenantFeature.setResetPeriod(systemFeature.getResetPeriod());
        tenantFeature.setValueScope(systemFeature.getValueScope());
        tenantFeature.setStatus(FeatureStatus.ACTIVE);

        return createFeature(tenantFeature);
    }

    /**
     * 批量激活功能
     */
    @Transactional
    public void activateFeatures(List<Long> featureIds) {
        if (featureIds == null || featureIds.isEmpty()) {
            return;
        }

        log.info("批量激活功能: count={}", featureIds.size());

        for (Long featureId : featureIds) {
            try {
                activateFeature(featureId);
            } catch (Exception e) {
                log.error("激活功能失败: id={}, error={}", featureId, e.getMessage());
                // 可以选择继续处理其他功能，或者抛出异常回滚整个事务
            }
        }

        log.info("批量激活功能完成: count={}", featureIds.size());
    }

    /**
     * 批量禁用功能
     */
    @Transactional
    public void deactivateFeatures(List<Long> featureIds) {
        if (featureIds == null || featureIds.isEmpty()) {
            return;
        }

        log.info("批量禁用功能: count={}", featureIds.size());

        for (Long featureId : featureIds) {
            try {
                deactivateFeature(featureId);
            } catch (Exception e) {
                log.error("禁用功能失败: id={}, error={}", featureId, e.getMessage());
                // 可以选择继续处理其他功能，或者抛出异常回滚整个事务
            }
        }

        log.info("批量禁用功能完成: count={}", featureIds.size());
    }

    /**
     * 验证功能数据
     */
    private void validateFeature(BillFeature feature) {
        if (feature == null) {
            throw new IllegalArgumentException("功能对象不能为空");
        }

        if (!feature.isValidConfiguration()) {
            throw new IllegalArgumentException("功能数据无效");
        }
    }
}
