# 计费系统设计方案

本文件梳理计费系统“套餐/功能/定价/订阅”核心建模，聚焦国内主流“套餐 + 权益”能力。

## 1.1 功能

```sql
CREATE TABLE `bill_feature` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `code` VARCHAR(50) NOT NULL COMMENT '功能代码',
  `name` VARCHAR(100) NOT NULL COMMENT '功能名称',
  `description` TEXT COMMENT '功能描述',
  `type` ENUM('QUOTA','USAGE','SWITCH') NOT NULL COMMENT '功能类型',
  `unit` VARCHAR(20) COMMENT '计量单位',
  `reset_period` ENUM('DAY','MONTH','YEAR','NEVER') NOT NULL DEFAULT 'MONTH' COMMENT '重置周期',
  `value_scope` ENUM('PER_SUBSCRIPTION','PER_SEAT') DEFAULT 'PER_SUBSCRIPTION' COMMENT '功能范围',
  `status` ENUM('ACTIVE','INACTIVE') DEFAULT 'ACTIVE',
  
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_type_status` (`type`, `status`),
  KEY `idx_reset_period` (`reset_period`, `type`)
) COMMENT='功能表';

/*
type 详细说明：

1. QUOTA（配额限制型）
   - 特点：有固定的使用上限，不按使用量计费
   - 示例：存储空间(100GB)、API调用次数(10000次/月)、用户数量(50个)
   - 计费方式：包含在套餐基础费用中
   - 配置：在 bill_plan_feature.quota_limit 中设置限额

2. USAGE（使用量计费型）
   - 特点：按实际使用量计费，可能有免费额度
   - 示例：CDN流量、短信发送、计算资源使用时长
   - 计费方式：超出免费额度后按使用量收费
   - 配置：在 bill_price 表中设置阶梯定价规则

3. SWITCH（开关功能型）
   - 特点：功能的开启/关闭，通常不涉及使用量
   - 示例：高级分析、白标定制、优先技术支持
   - 计费方式：开启后按周期收费（月费/年费）
   - 配置：在 bill_plan_feature.feature_value 中设置 'enabled'/'disabled'

reset_period 重要性说明：

1. 配额管理核心
   - API_CALLS + MONTH：每月1日重置API调用次数
   - STORAGE_SPACE + NEVER：存储空间不重置，累积使用
   - DATA_TRANSFER + MONTH：每月重置流量配额
   - USER_SEATS + NEVER：用户席位数不重置

2. 计费逻辑依赖
   - 重置周期决定超量计费的计算基准
   - 影响使用量统计的时间窗口
   - 与计费周期对齐，确保计费准确性

3. 系统自动化
   - 定时任务根据 reset_period 执行配额重置
   - 用户界面显示配额恢复时间
   - 告警系统基于重置周期预测配额耗尽时间

value_scope 功能范围说明：

1. PER_SUBSCRIPTION（按订阅）
   - 功能配额在整个订阅范围内共享
   - 示例：存储空间100GB，整个团队共享使用
   - 适用场景：团队共享资源、组织级功能

2. PER_SEAT（按席位）
   - 功能配额按每个用户席位独立计算
   - 示例：API调用1000次/月，每个用户独立拥有1000次
   - 适用场景：个人使用功能、用户级权限

使用示例：
- 团队存储: value_scope='PER_SUBSCRIPTION', quota_limit=100 (整个团队共享100GB)
- 个人API: value_scope='PER_SEAT', quota_limit=1000 (每个用户1000次/月)
- 团队项目: value_scope='PER_SUBSCRIPTION', quota_limit=10 (团队共享10个项目)
- 个人邮箱: value_scope='PER_SEAT', quota_limit=5 (每个用户5个邮箱账号)
*/
```

## 1.2 套餐
```sql
-- 原设计：bill_plan + bill_plan_version
-- 简化为：1个套餐表（版本作为字段）
CREATE TABLE `bill_plan` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `code` VARCHAR(50) NOT NULL COMMENT '套餐代码',
  `name` VARCHAR(100) NOT NULL COMMENT '套餐名称',
  `version` VARCHAR(20) NOT NULL DEFAULT 'v1.0' COMMENT '套餐版本号',
  `description` TEXT COMMENT '套餐描述',
  `plan_type` ENUM('FREE','BASIC','PRO','ENTERPRISE') NOT NULL COMMENT '套餐类型',
  `billing_mode` ENUM('PREPAID','POSTPAID','HYBRID') NOT NULL COMMENT '计费模式',
  `trial_enabled` TINYINT(1) DEFAULT 0 COMMENT '是否支持试用',
  `trial_days` INT DEFAULT 0 COMMENT '试用天数',
  `trial_limit_per_user` INT DEFAULT 1 COMMENT '每用户试用次数限制',
  `status` ENUM('DRAFT','ACTIVE','INACTIVE','DEPRECATED','ARCHIVED') DEFAULT 'DRAFT',
  `effective_time` DATETIME NOT NULL COMMENT '生效时间',
  `expire_time` DATETIME COMMENT '失效时间',
  
  UNIQUE KEY `uk_plan_code_version` (`code`, `version`),
  KEY `idx_plan_type_status` (`plan_type`, `status`),
  KEY `idx_trial_enabled` (`trial_enabled`, `status`)
) COMMENT='套餐表（含版本信息）';

/*
version 版本管理说明：

1. 版本并存机制
   - 同一套餐可以有多个版本同时存在
   - 示例：PRO v1.0, PRO v2.0 可以同时提供服务
   - 新用户订阅最新版本，老用户可继续使用旧版本
   - 支持平滑的版本升级和迁移

status 套餐状态说明：

1. DRAFT（草稿状态）
   - 特点：开发中，不对外开放
   - 使用场景：新套餐设计阶段，内部测试
   - 用户影响：用户无法看到和订阅

2. ACTIVE（生效中）
   - 特点：正常运营状态，用户可以订阅
   - 使用场景：主要销售的套餐版本
   - 用户影响：用户可以正常订阅和使用

3. INACTIVE（已禁用）
   - 特点：暂停新订阅，现有订阅继续服务
   - 使用场景：临时下架，问题修复期间
   - 用户影响：现有用户不受影响，新用户无法订阅

4. DEPRECATED（已弃用）
   - 特点：不推荐使用，逐步下线
   - 使用场景：老版本套餐的退出策略
   - 用户影响：现有用户可继续使用，建议升级

5. ARCHIVED（已归档）
   - 特点：仅作历史记录保存，完全停止服务
   - 使用场景：彻底下线的套餐版本
   - 用户影响：所有相关订阅需要迁移

billing_mode 计费模式说明：

1. PREPAID（预付费）
   - 特点：先充值后使用，余额不足时停服
   - 适用场景：个人用户、小型企业
   - 扣费逻辑：实时扣减账户余额

2. POSTPAID（后付费）
   - 特点：先使用后付款，定期生成账单
   - 适用场景：企业客户、信用良好的用户
   - 扣费逻辑：累计使用量，周期性结算

3. HYBRID（混合模式）
   - 特点：优先扣余额，不足部分记账
   - 适用场景：灵活的企业客户
   - 扣费逻辑：余额充足时预付费，不足时后付费

试用功能配置说明：

1. trial_enabled（试用开关）
   - trial_enabled=0：不支持试用，用户直接付费订阅
   - trial_enabled=1：支持试用，用户可以免费体验
   - 适用场景：付费套餐通常支持试用，免费套餐无需试用

2. trial_days（试用天数）
   - 具体数值：如 15 表示15天试用期
   - 常见配置：7天、15天、30天
   - 试用期内用户可以使用套餐的所有功能

3. trial_limit_per_user（试用次数限制）
   - 默认值：1，每个用户只能试用一次
   - 特殊配置：0表示无限制试用（通常不推荐）
   - 防止用户重复试用同一套餐

使用示例：
- 基础版: trial_enabled=1, trial_days=7, trial_limit_per_user=1
- 专业版: trial_enabled=1, trial_days=15, trial_limit_per_user=1
- 企业版: trial_enabled=1, trial_days=30, trial_limit_per_user=1
- 免费版: trial_enabled=0, trial_days=0, trial_limit_per_user=0
*/

-- 套餐功能关联表
CREATE TABLE `bill_plan_feature` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `plan_id` BIGINT NOT NULL COMMENT '套餐ID',
  `feature_id` BIGINT NOT NULL COMMENT '功能ID',
  `feature_value` VARCHAR(200) COMMENT '功能值配置',
  `status` ENUM('ACTIVE','INACTIVE') DEFAULT 'ACTIVE',
  
  UNIQUE KEY `uk_plan_feature` (`plan_id`, `feature_id`),
  KEY `idx_feature_id` (`feature_id`),
  FOREIGN KEY (`plan_id`) REFERENCES `bill_plan`(`id`),
  FOREIGN KEY (`feature_id`) REFERENCES `bill_feature`(`id`)
) COMMENT='套餐功能';

/*
feature_value 功能值配置说明：

1. QUOTA类型功能
   - 配置方式：具体数值，如 "100" 表示100GB存储
   - 示例：存储空间="100", 用户数量="50", 项目数量="10"
   - 含义：该套餐提供的配额限制

2. USAGE类型功能
   - 配置方式：免费额度，如 "1000" 表示1000次免费调用
   - 示例：API调用="1000", 短信发送="500", 邮件发送="2000"
   - 含义：超出此额度后开始按量计费

3. SWITCH类型功能
   - 配置方式：开关状态，"enabled" 或 "disabled"
   - 示例：高级分析="enabled", 白标定制="disabled"
   - 含义：功能是否对该套餐开放

*/
```

## 1.3 试用记录表
```sql
-- 试用记录表（跟踪用户试用历史）
CREATE TABLE `bill_trial_record` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `plan_id` BIGINT NOT NULL COMMENT '套餐ID',
  `trial_start_time` DATETIME NOT NULL COMMENT '试用开始时间',
  `trial_end_time` DATETIME NOT NULL COMMENT '试用结束时间',
  `status` ENUM('ACTIVE','EXPIRED','CONVERTED','CANCELLED') DEFAULT 'ACTIVE' COMMENT '试用状态',
  `converted_time` DATETIME COMMENT '转换为付费时间',
  `cancel_reason` VARCHAR(200) COMMENT '取消原因',
  
  KEY `idx_tenant_plan` (`tenant_id`, `plan_id`),
  KEY `idx_trial_time` (`trial_start_time`, `trial_end_time`),
  KEY `idx_status` (`status`),
  FOREIGN KEY (`plan_id`) REFERENCES `bill_plan`(`id`)
) COMMENT='试用记录表';

/*
status 试用状态说明：

1. ACTIVE（试用中）
   - 特点：试用期内，用户正在使用套餐功能
   - 系统行为：提供完整的套餐功能
   - 用户体验：可以正常使用所有功能

2. EXPIRED（已过期）
   - 特点：试用期结束，未转换为付费用户
   - 系统行为：停止提供套餐功能，降级到免费版
   - 用户体验：功能受限，提示升级

3. CONVERTED（已转换）
   - 特点：试用期内或结束后转换为付费用户
   - 系统行为：继续提供套餐功能，开始正常计费
   - 用户体验：无缝过渡到付费服务

4. CANCELLED（已取消）
   - 特点：用户主动取消试用
   - 系统行为：立即停止提供套餐功能
   - 用户体验：回到试用前状态

试用业务逻辑：
- 用户申请试用时，检查 trial_limit_per_user 限制
- 创建试用记录，状态为 ACTIVE
- 试用期结束时，自动更新状态为 EXPIRED
- 用户付费订阅时，更新状态为 CONVERTED
*/
```

## 1.4 统一定价表
```sql
CREATE TABLE `bill_price` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `type` ENUM('PLAN','FEATURE','TENANT_PLAN','TENANT_FEATURE') NOT NULL COMMENT '定价类型',
  `target_type` ENUM('PLAN','FEATURE') NOT NULL COMMENT '目标类型',
  `target_id` BIGINT NOT NULL COMMENT '目标ID',
  `tenant_id` BIGINT COMMENT '租户ID',
  `price` DECIMAL(18,4) DEFAULT 0 COMMENT '价格：MONTHLY/YEARLY时表示固定费用金额（月费或者年费），USAGE时通常为0（实际单价在pricing_config中配置）',
  `currency` VARCHAR(10) DEFAULT 'USD' COMMENT '货币单位',
  `billing_cycle` ENUM('MONTHLY','YEARLY','USAGE') NOT NULL COMMENT '计费周期：MONTHLY/YEARLY为固定费用，USAGE为按量计费',
  `pricing_config` JSON COMMENT '定价配置：仅当billing_cycle=USAGE时必填，支持quota/tiered/tiered_fixed/usage/package等模式',
  `effective_time` DATETIME NOT NULL COMMENT '生效时间',
  `expire_time` DATETIME COMMENT '失效时间',
  `status` ENUM('DRAFT','ACTIVE','INACTIVE','EXPIRED') DEFAULT 'DRAFT' COMMENT '状态',
  
  UNIQUE KEY `uk_pricing_rule` (`type`, `target_type`, `target_id`, `tenant_id`, `billing_cycle`, `effective_time`),
  KEY `idx_type_target` (`type`, `target_type`, `target_id`),
  KEY `idx_tenant_pricing` (`tenant_id`, `type`),
  KEY `idx_effective_time` (`effective_time`, `expire_time`),
  KEY `idx_status` (`status`)
) COMMENT='统一定价表';

/*
type 定价类型说明：

1. PLAN（标准套餐定价）
   - 特点：适用于所有租户的套餐定价
   - 条件：tenant_id = NULL
   - 示例：PRO套餐月费99元，适用于所有用户

2. FEATURE（标准功能定价）
   - 特点：适用于所有租户的功能定价
   - 条件：tenant_id = NULL
   - 示例：API调用超量费用0.001元/次

3. TENANT_PLAN（租户专属套餐定价）
   - 特点：特定租户的套餐优惠价格
   - 条件：tenant_id 有具体值
   - 示例：大客户PRO套餐优惠价79元/月

4. TENANT_FEATURE（租户专属功能定价）
   - 特点：特定租户的功能优惠价格
   - 条件：tenant_id 有具体值
   - 示例：VIP客户API调用优惠价0.0008元/次

target_type + target_id 目标对象说明：

1. target_type = 'PLAN'
   - target_id 指向 bill_plan.id
   - 用于套餐相关的定价规则
   - 配合 type 的 PLAN 或 TENANT_PLAN

2. target_type = 'FEATURE'
   - target_id 指向 bill_feature.id
   - 用于功能相关的定价规则
   - 配合 type 的 FEATURE 或 TENANT_FEATURE

price 价格字段说明：

1. billing_cycle = 'MONTHLY'
   - price 表示月费，如 99.00 元/月
   - 适用场景：套餐月度订阅费用

2. billing_cycle = 'YEARLY'
   - price 表示年费，如 999.00 元/年
   - 适用场景：套餐年度订阅费用

3. billing_cycle = 'USAGE'
   - price 表示单价，如 0.001 元/次
   - 适用场景：按使用量计费的功能

status 定价状态说明：

1. DRAFT（草稿状态）
   - 特点：定价规则未生效，不参与计费
   - 使用场景：新定价策略的准备阶段

2. ACTIVE（生效中）
   - 特点：正常参与计费计算
   - 使用场景：当前使用的定价规则

3. INACTIVE（已禁用）
   - 特点：暂时停用，不参与计费
   - 使用场景：临时调整定价策略

4. EXPIRED（已过期）
   - 特点：自动失效，系统自动设置
   - 使用场景：到期的限时定价活动

billing_cycle 计费周期说明：

1. MONTHLY（月度计费）
   - 特点：固定月费，每月收取固定金额
   - price字段：表示月费金额，如99.00元/月
   - pricing_config：通常为NULL，不需要使用量配置
   - 适用场景：套餐基础月费、月度订阅服务

2. YEARLY（年度计费）
   - 特点：固定年费，每年收取固定金额
   - price字段：表示年费金额，如999.00元/年
   - pricing_config：通常为NULL，不需要使用量配置
   - 适用场景：套餐基础年费、年度订阅优惠

3. USAGE（使用量计费）
   - 特点：按实际使用量计费，支持多种定价模式
   - price字段：通常为0，实际单价在pricing_config中配置
   - pricing_config：必须配置，定义具体的计费规则
   - 适用场景：API调用、存储空间、流量等按量计费功能

pricing_config JSON配置详细说明（统一格式）：

**统一JSON格式结构：**
{
  "type": "quota|tiered|usage|package|tiered_fixed",
  "values": [
    {
      "min": 0,
      "max": 1000,
      "quantity": 10000,  // 仅package类型使用
      "price": 0.001
    }
  ]
}

1. 免费配额模式（quota）
   - 配置：{"type": "quota", "values": [{"min": 0, "max": 1000, "price": 0}, {"min": 1001, "max": null, "price": 0.001}]}
   - 含义：0-1000次免费，1001次以上每次0.001元
   - 计费方式：免费额度内不计费，超出部分按单价计费
   - 适用场景：套餐包含免费额度的功能

2. 阶梯定价模式（tiered）
   - 配置：{"type": "tiered", "values": [{"min": 0, "max": 1000, "price": 0}, {"min": 1001, "max": 5000, "price": 0.001}, {"min": 5001, "max": null, "price": 0.0008}]}
   - 含义：0-1000次免费，1001-5000次每次0.001元，5001次以上每次0.0008元
   - 计费方式：每个阶梯内按对应单价计费
   - 适用场景：使用量越大单价越低的优惠策略

3. 纯按量计费模式（usage）
   - 配置：{"type": "usage", "values": [{"min": 0, "max": null, "price": 0.001}]}
   - 含义：每次使用0.001元，无免费额度
   - 计费方式：所有使用量都按单价计费
   - 适用场景：简单的按量付费功能

4. 包量计费模式（package）
   - 配置：{"type": "package", "values": [{"quantity": 10000, "price": 50}, {"quantity": 50000, "price": 200}]}
   - 含义：10000次包50元，50000次包200元
   - 计费方式：用户购买包，包内使用量不额外收费
   - 适用场景：预付费包量服务

5. 固定价格阶梯模式（tiered_fixed）
   - 配置：{"type": "tiered_fixed", "values": [{"min": 0, "max": 1000, "price": 0}, {"min": 1001, "max": 5000, "price": 10.0}, {"min": 5001, "max": null, "price": 30.0}]}
   - 含义：0-1000次免费，1001-5000次固定收费10元，5001次以上固定收费30元
   - 计费方式：达到阶梯后按固定价格收费，不按单次计费
   - 适用场景：大客户专属定价，提供更优惠的包段价格

**统一格式字段说明：**
- type: 定价类型标识
- values: 定价配置数组（统一字段名，适用于所有定价类型）
- min/max: 使用量范围，max为null表示无上限
- quantity: 包量计费专用字段
- price: 价格金额

**配置规则总结：**
- billing_cycle = 'MONTHLY/YEARLY' 时，pricing_config 通常为 NULL
- billing_cycle = 'USAGE' 时，pricing_config 必须配置且包含 type 和 values 字段
- 所有定价类型都使用统一的 values 数组结构
*/

```

## 1.5 订阅表
```sql
CREATE TABLE `bill_subscription` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `sub_no` VARCHAR(50) NOT NULL COMMENT '订阅编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `plan_id` BIGINT NOT NULL COMMENT '套餐ID',
  `remark` VARCHAR(512) COMMENT '备注信息',

  `pricing_snapshot` JSON NOT NULL COMMENT '价格快照：保存订阅时的完整定价信息',
  
  `quantity` INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '订阅席位数量：用于多席位计费和PER_SEAT功能配额计算',
  `start_time` DATETIME NOT NULL COMMENT '订阅开始时间',
  `end_time` DATETIME COMMENT '订阅结束时间',
  `current_period_start_time` DATETIME NOT NULL COMMENT '当前计费周期开始时间',
  `current_period_end_time` DATETIME NOT NULL COMMENT '当前计费周期结束时间',
  `next_billing_time` DATETIME COMMENT '下次计费时间',
  `status` ENUM('TRIAL','ACTIVE','PAST_DUE','SUSPENDED','CANCELLED','EXPIRED') DEFAULT 'ACTIVE' COMMENT '订阅状态',
  `auto_renew` TINYINT(1) DEFAULT 1 COMMENT '是否自动续费',
  `cancel_at_period_end` TINYINT(1) DEFAULT 0 COMMENT '是否在周期结束时取消',
    
  UNIQUE KEY `uk_sub_no` (`sub_no`),
  KEY `idx_tenant_status_period` (`tenant_id`, `status`, `current_period_end_time`),
  KEY `idx_plan_id` (`plan_id`),
  KEY `idx_next_billing` (`next_billing_time`, `auto_renew`),


  FOREIGN KEY (`plan_id`) REFERENCES `bill_plan`(`id`)
) COMMENT='订阅记录表';

/*
字段说明：

1. 价格快照字段（必须保存）：
   - pricing_snapshot: 订阅时的完整价格快照，包含所有定价信息

pricing_snapshot JSON结构示例：
{
  "plan_pricing": {
    "type": "PLAN",
    "target_type": "PLAN", 
    "target_id": 2,
    "price": 99.00,
    "currency": "USD",
    "billing_cycle": "MONTHLY",
    "pricing_config": null
  },
  "feature_pricing": [
    {
      "type": "FEATURE",
      "target_type": "FEATURE",
      "target_id": 1,
      "code": "api_calls",
      "price": 0,
      "currency": "USD", 
      "billing_cycle": "USAGE",
      "pricing_config": {
        "type": "quota",
        "values": [
          {"min": 0, "max": 10000, "price": 0},
          {"min": 10001, "max": null, "price": 0.001}
        ]
      }
    }
  ]
}

2. 订阅配置字段：
   - quantity: 订阅席位数量，用于多席位计费和功能配额计算

3. 时间字段：
   - start_time: 订阅开始时间
   - end_time: 订阅结束时间（NULL表示无限期）
   - current_period_start_time/current_period_end_time: 当前计费周期的开始和结束时间
   - next_billing_time: 下次计费时间

4. 状态字段：
   - status: 订阅状态（TRIAL/ACTIVE/PAST_DUE/SUSPENDED/CANCELLED/EXPIRED）
   - auto_renew: 是否自动续费
   - cancel_at_period_end: 是否在当前周期结束时取消（用于优雅取消）


订阅状态详细说明：

1. TRIAL（试用中）
   - 特点：用户正在试用套餐，未付费
   - 使用场景：试用期内，提供完整功能
   - 转换：试用结束后可转为ACTIVE（付费）或EXPIRED（过期）

2. ACTIVE（活跃中）
   - 特点：正常付费订阅，服务正常
   - 使用场景：用户已付费，享受完整服务
   - 转换：可转为PAST_DUE（逾期）、SUSPENDED（暂停）、CANCELLED（取消）

3. PAST_DUE（逾期未付）
   - 特点：付费失败，但仍在宽限期内
   - 使用场景：给用户时间更新付费方式
   - 转换：付费成功转ACTIVE，超过宽限期转SUSPENDED或EXPIRED

4. SUSPENDED（已暂停）
   - 特点：服务暂停，但订阅关系保留
   - 使用场景：长期逾期、违规、主动暂停等
   - 转换：问题解决后可转回ACTIVE

5. CANCELLED（已取消）
   - 特点：用户主动取消，但服务继续到周期结束
   - 使用场景：优雅取消，避免立即中断服务
   - 转换：周期结束后转为EXPIRED

6. EXPIRED（已过期）
   - 特点：订阅已结束，服务完全停止
   - 使用场景：试用过期、订阅到期、取消后到期
   - 转换：用户重新订阅可转为ACTIVE或TRIAL

业务场景说明：

1. 试用订阅：
   - status = 'TRIAL'
   - 试用信息存储在 bill_trial_record 表中
   - start_time = 试用开始时间
   - 试用结束：转为ACTIVE（付费）或EXPIRED（过期）

2. 直接订阅（无试用）：
   - status = 'ACTIVE'
   - start_time = 订阅开始时间
   - current_period_start_time = start_time, current_period_end_time = start_time + billing_cycle

3. 试用转正式订阅：
   - 先创建TRIAL状态订阅
   - 试用转换时：status = 'ACTIVE', 更新计费信息
   - 试用历史记录保留在 bill_trial_record 表中

4. 付费失败处理：
   - 付费失败：status = 'PAST_DUE'
   - 宽限期内可继续使用服务
   - 超过宽限期：status = 'SUSPENDED' 或 'EXPIRED'

5. 优雅取消订阅：
   - 用户取消时：status = 'CANCELLED', cancel_at_period_end = 1, auto_renew = 0
   - 订阅继续到 current_period_end_time，然后自动变为 'EXPIRED'
   - 避免立即中断服务

试用信息管理：
- 试用相关信息统一存储在 bill_trial_record 表中
- bill_subscription 表只保留订阅状态（TRIAL/ACTIVE等）
- 避免数据冗余，确保试用信息的完整性和一致性
- 支持一个用户多次试用不同套餐的场景

4. 计费周期管理：
   - 每次计费后，更新 current_period_start_time, current_period_end_time, next_billing_time
   - current_period_start_time/current_period_end_time 用于确定当前服务周期
   - next_billing_time 用于定时任务触发下次计费

quantity 字段的重要作用：

1. 多席位计费：
   - 套餐单价：$99/月/席位
   - 订阅10个席位：quantity = 10
   - 总费用：$99 × 10 = $990/月

2. PER_SEAT 功能配额计算：
   - 功能配置：API调用 1000次/月/席位 (value_scope='PER_SEAT')
   - 订阅10个席位：总配额 = 1000 × 10 = 10000次/月
   - 每个用户独立享有1000次配额

3. PER_SUBSCRIPTION 功能配额：
   - 功能配置：存储空间 100GB/月 (value_scope='PER_SUBSCRIPTION')
   - 不管多少席位：总配额 = 100GB/月（团队共享）

4. 席位数量变更：
   - 支持动态调整席位数量
   - 按比例计费：增加席位按剩余天数计费
   - 功能配额实时调整

*/
```

## 1.6 使用量记录表
```sql
CREATE TABLE `bill_usage` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `subscription_id` BIGINT NOT NULL COMMENT '订阅ID',
  `feature_id` BIGINT NOT NULL COMMENT '功能ID',
  `usage_time` DATETIME NOT NULL COMMENT '使用时间',
  `usage_amount` DECIMAL(18,4) NOT NULL COMMENT '使用量',
  `unit` VARCHAR(20) COMMENT '计量单位',
  `billing_period` DATE NOT NULL COMMENT '计费周期（YYYY-MM-DD格式，表示所属的计费月份）',
  `metadata` JSON COMMENT '使用量元数据：记录详细的使用信息',
  
  KEY `idx_tenant_subscription` (`tenant_id`, `subscription_id`),
  KEY `idx_feature_billing_period` (`feature_id`, `billing_period`),
  KEY `idx_usage_time` (`usage_time`),
  KEY `idx_billing_period` (`billing_period`),
  KEY `idx_subscription_feature_period` (`subscription_id`, `feature_id`, `billing_period`),
  FOREIGN KEY (`subscription_id`) REFERENCES `bill_subscription`(`id`),
  FOREIGN KEY (`feature_id`) REFERENCES `bill_feature`(`id`)
) COMMENT='使用量记录表';

/*
字段说明：

1. 核心字段：
   - tenant_id: 租户ID，用于数据隔离
   - subscription_id: 订阅ID，关联具体的订阅
   - feature_id: 功能ID，标识使用的功能
   - usage_amount: 使用量，如API调用次数、存储空间GB数

2. 时间字段：
   - usage_time: 具体的使用时间点
   - billing_period: 计费周期，格式为YYYY-MM-DD，表示该使用量归属的计费月份

3. 元数据字段：
   - metadata: JSON格式，记录详细的使用信息
   - 示例：{"api_endpoint": "/api/users", "response_time": 120, "status_code": 200}

使用场景：

1. API调用记录：
   - feature_id: API功能ID
   - usage_amount: 1（每次调用记录1次）
   - metadata: {"endpoint": "/api/users", "method": "GET", "status": 200}

2. 存储使用记录：
   - feature_id: 存储功能ID
   - usage_amount: 1.5（使用1.5GB存储）
   - metadata: {"file_type": "image", "file_size": 1572864}

3. 流量使用记录：
   - feature_id: 流量功能ID
   - usage_amount: 100.5（使用100.5MB流量）
   - metadata: {"region": "us-east-1", "cdn_hit": false}

billing_period 重要性：
- 用于按月汇总使用量
- 支持跨月的使用量统计
- 便于生成月度账单
- 格式：'2024-01-01' 表示2024年1月的使用量

索引设计说明：

1. idx_tenant_subscription: 租户级别的使用量查询
2. idx_feature_billing_period: 按功能和计费周期统计
3. idx_usage_time: 按时间范围查询使用量
4. idx_billing_period: 按计费周期汇总
5. idx_subscription_feature_period: 订阅功能使用量统计（最重要的复合索引）

查询示例：

-- 查询某订阅在特定月份的API使用量
SELECT SUM(usage_amount) as total_usage
FROM bill_usage 
WHERE subscription_id = 12345 
  AND feature_id = 1 
  AND billing_period = '2024-01-01';

-- 查询租户在某月的所有使用量
SELECT f.name, SUM(u.usage_amount) as total_usage
FROM bill_usage u
JOIN bill_feature f ON u.feature_id = f.id
WHERE u.tenant_id = 1001 
  AND u.billing_period = '2024-01-01'
GROUP BY f.id, f.name;

-- 查询订阅的实时使用量（当前月）
SELECT f.name, SUM(u.usage_amount) as current_usage
FROM bill_usage u
JOIN bill_feature f ON u.feature_id = f.id
WHERE u.subscription_id = 12345
  AND u.billing_period = DATE_FORMAT(NOW(), '%Y-%m-01')
GROUP BY f.id, f.name;

数据分区建议：
- 按 billing_period 进行月度分区
- 提高大数据量下的查询性能
- 便于历史数据的归档和清理

性能优化：
- 使用批量插入减少IO开销
- 定期汇总历史数据到统计表
- 对于高频使用的功能，考虑异步写入
*/

```

## 1.7 账单表
```sql
CREATE TABLE `bill_invoice` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `invoice_no` VARCHAR(50) NOT NULL COMMENT '账单编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `subscription_id` BIGINT NOT NULL COMMENT '订阅ID',
  `billing_period_start` DATE NOT NULL COMMENT '计费周期开始日期',
  `billing_period_end` DATE NOT NULL COMMENT '计费周期结束日期',
  `invoice_type` ENUM('SUBSCRIPTION','USAGE','ADJUSTMENT','REFUND') NOT NULL COMMENT '账单类型',
  
  -- 金额信息
  `subtotal` DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '小计金额',
  `tax_amount` DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '税费金额',
  `discount_amount` DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '折扣金额',
  `total_amount` DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '总金额',
  `currency` VARCHAR(10) DEFAULT 'USD' COMMENT '货币单位',
  
  -- 状态信息
  `status` ENUM('DRAFT','PENDING','PAID','OVERDUE','CANCELLED','REFUNDED') DEFAULT 'DRAFT' COMMENT '账单状态',
  `due_date` DATETIME NOT NULL COMMENT '到期时间',
  `paid_time` DATETIME COMMENT '支付时间',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  
  UNIQUE KEY `uk_invoice_no` (`invoice_no`),
  KEY `idx_tenant_subscription` (`tenant_id`, `subscription_id`),
  KEY `idx_billing_period` (`billing_period_start`, `billing_period_end`),
  KEY `idx_status_due_date` (`status`, `due_date`),
  KEY `idx_invoice_type` (`invoice_type`),
  FOREIGN KEY (`subscription_id`) REFERENCES `bill_subscription`(`id`)
) COMMENT='账单表';

/*
invoice_type 账单类型说明：

1. SUBSCRIPTION（订阅账单）
   - 特点：套餐基础费用账单
   - 生成时机：每个计费周期开始时
   - 金额来源：套餐的月费或年费

2. USAGE（使用量账单）
   - 特点：按量计费功能的使用费用
   - 生成时机：计费周期结束后，根据实际使用量生成
   - 金额来源：超出免费额度的使用量费用

3. ADJUSTMENT（调整账单）
   - 特点：价格调整、升级降级等产生的差额
   - 生成时机：套餐变更、价格调整时
   - 金额来源：新旧价格的差额

4. REFUND（退款账单）
   - 特点：退款记录
   - 生成时机：用户申请退款时
   - 金额来源：负数，表示退还给用户的金额

status 账单状态说明：

1. DRAFT（草稿状态）
   - 特点：账单生成中，尚未确认
   - 用户影响：用户看不到此账单

2. PENDING（待支付）
   - 特点：账单已确认，等待用户支付
   - 用户影响：用户可以看到并支付此账单

3. PAID（已支付）
   - 特点：用户已成功支付
   - 用户影响：服务正常，账单已结清

4. OVERDUE（逾期未付）
   - 特点：超过到期时间仍未支付
   - 用户影响：可能影响服务使用

5. CANCELLED（已取消）
   - 特点：账单被取消，无需支付
   - 用户影响：账单作废

6. REFUNDED（已退款）
   - 特点：账单金额已退还给用户
   - 用户影响：金额已退回
*/

```

## 1.8 账单明细表
```sql
CREATE TABLE `bill_invoice_item` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `invoice_id` BIGINT NOT NULL COMMENT '账单ID',
  `item_type` ENUM('PLAN','FEATURE','DISCOUNT','TAX','ADJUSTMENT') NOT NULL COMMENT '明细类型',
  `target_type` ENUM('PLAN','FEATURE') COMMENT '目标类型',
  `target_id` BIGINT COMMENT '目标ID',
  `description` VARCHAR(200) NOT NULL COMMENT '明细描述',
  `quantity` DECIMAL(18,4) NOT NULL DEFAULT 1 COMMENT '数量',
  `unit_price` DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '单价',
  `amount` DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '金额',
  `currency` VARCHAR(10) DEFAULT 'USD' COMMENT '货币单位',
  `billing_period_start` DATE COMMENT '计费周期开始',
  `billing_period_end` DATE COMMENT '计费周期结束',
  `metadata` JSON COMMENT '明细元数据',
  
  KEY `idx_invoice_id` (`invoice_id`),
  KEY `idx_item_type` (`item_type`),
  KEY `idx_target` (`target_type`, `target_id`),
  FOREIGN KEY (`invoice_id`) REFERENCES `bill_invoice`(`id`)
) COMMENT='账单明细表';

/*
item_type 明细类型说明：

1. PLAN（套餐费用）
   - target_type: 'PLAN'
   - target_id: 套餐ID
   - description: "PRO套餐月费"
   - quantity: 1
   - unit_price: 99.00
   - amount: 99.00

2. FEATURE（功能费用）
   - target_type: 'FEATURE'
   - target_id: 功能ID
   - description: "API调用超量费用"
   - quantity: 5000（超量5000次）
   - unit_price: 0.001
   - amount: 5.00

3. DISCOUNT（折扣）
   - target_type: NULL
   - target_id: NULL
   - description: "新用户优惠"
   - quantity: 1
   - unit_price: -10.00
   - amount: -10.00

4. TAX（税费）
   - target_type: NULL
   - target_id: NULL
   - description: "增值税"
   - quantity: 1
   - unit_price: 8.91
   - amount: 8.91

5. ADJUSTMENT（调整）
   - target_type: 'PLAN'
   - target_id: 套餐ID
   - description: "套餐升级差额"
   - quantity: 1
   - unit_price: 50.00
   - amount: 50.00

metadata 元数据示例：
{
  "usage_period": "2024-01",
  "free_quota": 10000,
  "total_usage": 15000,
  "billable_usage": 5000,
  "pricing_config": {
    "type": "quota",
    "values": [
      {"min": 0, "max": 10000, "price": 0},
      {"min": 10001, "max": null, "price": 0.001}
    ]
  }
}
*/
```

## 1.9 支付记录表
```sql
CREATE TABLE `bill_payment` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `payment_no` VARCHAR(50) NOT NULL COMMENT '支付编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `invoice_id` BIGINT COMMENT '账单ID',
  `amount` DECIMAL(18,4) NOT NULL COMMENT '支付金额',
  `currency` VARCHAR(10) DEFAULT 'USD' COMMENT '货币单位',
  `payment_method` ENUM('CREDIT_CARD','DEBIT_CARD','PAYPAL','BANK_TRANSFER','WALLET','OTHER') NOT NULL COMMENT '支付方式',
  `payment_gateway` VARCHAR(50) COMMENT '支付网关',
  `gateway_transaction_id` VARCHAR(100) COMMENT '网关交易ID',
  `status` ENUM('PENDING','SUCCESS','FAILED','CANCELLED','REFUNDED') DEFAULT 'PENDING' COMMENT '支付状态',
  `paid_time` DATETIME COMMENT '支付时间',
  `failure_reason` VARCHAR(200) COMMENT '失败原因',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  
  UNIQUE KEY `uk_payment_no` (`payment_no`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_invoice_id` (`invoice_id`),
  KEY `idx_status_paid_time` (`status`, `paid_time`),
  KEY `idx_gateway_transaction` (`payment_gateway`, `gateway_transaction_id`),
  FOREIGN KEY (`invoice_id`) REFERENCES `bill_invoice`(`id`)
) COMMENT='支付记录表';

/*
payment_method 支付方式说明：

1. CREDIT_CARD（信用卡）
   - 最常用的在线支付方式
   - 支持自动续费

2. DEBIT_CARD（借记卡）
   - 直接从银行账户扣款
   - 支持自动续费

3. PAYPAL（PayPal）
   - 第三方支付平台
   - 支持自动续费

4. BANK_TRANSFER（银行转账）
   - 企业客户常用
   - 通常需要手动处理

5. WALLET（电子钱包）
   - 如支付宝、微信支付等
   - 适用于特定地区

6. OTHER（其他）
   - 其他支付方式
   - 需要在 metadata 中记录具体方式

status 支付状态说明：

1. PENDING（处理中）
   - 支付请求已提交，等待处理结果

2. SUCCESS（成功）
   - 支付成功完成

3. FAILED（失败）
   - 支付失败，需要重新支付

4. CANCELLED（已取消）
   - 用户主动取消支付

5. REFUNDED（已退款）
   - 支付已退款给用户
*/
```

## 2. 示例数据和查询

### 2.1 基础数据示例

```sql
-- 插入功能数据
INSERT INTO bill_feature (code, name, type, unit, reset_period, value_scope) VALUES
('api_calls', 'API调用', 'USAGE', '次', 'MONTH', 'PER_SUBSCRIPTION'),
('storage', '存储空间', 'QUOTA', 'GB', 'NEVER', 'PER_SUBSCRIPTION'),
('users', '用户数量', 'QUOTA', '个', 'NEVER', 'PER_SUBSCRIPTION'),
('advanced_analytics', '高级分析', 'SWITCH', NULL, 'NEVER', 'PER_SUBSCRIPTION');

-- 插入套餐数据
INSERT INTO bill_plan (code, name, plan_type, billing_mode, trial_enabled, trial_days, status, effective_time) VALUES
('FREE', '免费版', 'FREE', 'PREPAID', 0, 0, 'ACTIVE', NOW()),
('PRO', '专业版', 'PRO', 'POSTPAID', 1, 15, 'ACTIVE', NOW()),
('ENTERPRISE', '企业版', 'ENTERPRISE', 'POSTPAID', 1, 30, 'ACTIVE', NOW());

-- 插入套餐功能配置
INSERT INTO bill_plan_feature (plan_id, feature_id, feature_value) VALUES
-- 免费版配置
(1, 1, '1000'),    -- API调用：1000次/月
(1, 2, '1'),       -- 存储：1GB
(1, 3, '1'),       -- 用户：1个
(1, 4, 'disabled'), -- 高级分析：关闭
-- 专业版配置
(2, 1, '10000'),   -- API调用：10000次/月
(2, 2, '100'),     -- 存储：100GB
(2, 3, '10'),      -- 用户：10个
(2, 4, 'enabled'), -- 高级分析：开启
-- 企业版配置
(3, 1, '100000'),  -- API调用：100000次/月
(3, 2, '1000'),    -- 存储：1000GB
(3, 3, '100'),     -- 用户：100个
(3, 4, 'enabled'); -- 高级分析：开启

-- 插入定价数据
INSERT INTO bill_price (type, target_type, target_id, price, currency, billing_cycle, status, effective_time) VALUES
-- 套餐定价
('PLAN', 'PLAN', 2, 99.00, 'USD', 'MONTHLY', 'ACTIVE', NOW()),
('PLAN', 'PLAN', 2, 999.00, 'USD', 'YEARLY', 'ACTIVE', NOW()),
('PLAN', 'PLAN', 3, 299.00, 'USD', 'MONTHLY', 'ACTIVE', NOW()),
('PLAN', 'PLAN', 3, 2999.00, 'USD', 'YEARLY', 'ACTIVE', NOW());

-- 功能超量定价
INSERT INTO bill_price (type, target_type, target_id, price, currency, billing_cycle, pricing_config, status, effective_time) VALUES
('FEATURE', 'FEATURE', 1, 0, 'USD', 'USAGE', 
 '{"type": "quota", "values": [{"min": 0, "max": 10000, "price": 0}, {"min": 10001, "max": null, "price": 0.001}]}', 
 'ACTIVE', NOW());
```

### 2.2 订阅创建示例

```sql
-- 创建试用订阅（包含完整价格快照）
INSERT INTO bill_subscription (
  sub_no, tenant_id, plan_id, pricing_snapshot,
  start_time, current_period_start_time, current_period_end_time, status
) VALUES (
  'SUB-2024-001', 1001, 2, 
  '{"plan_pricing":{"type":"PLAN","target_type":"PLAN","target_id":2,"price":99.00,"currency":"USD","billing_cycle":"MONTHLY","pricing_config":null},"feature_pricing":[{"type":"FEATURE","target_type":"FEATURE","target_id":1,"feature_code":"api_calls","price":0,"currency":"USD","billing_cycle":"USAGE","pricing_config":{"type":"quota","values":[{"min":0,"max":10000,"price":0},{"min":10001,"max":null,"price":0.001}]}}]}',
  NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 15 DAY), 'TRIAL'
);

-- 创建直接付费订阅
INSERT INTO bill_subscription (
  sub_no, tenant_id, plan_id, pricing_snapshot,
  start_time, current_period_start_time, current_period_end_time, next_billing_time, status
) VALUES (
  'SUB-2024-002', 1002, 2,
  '{"plan_pricing":{"type":"PLAN","target_type":"PLAN","target_id":2,"price":99.00,"currency":"USD","billing_cycle":"MONTHLY","pricing_config":null},"feature_pricing":[{"type":"FEATURE","target_type":"FEATURE","target_id":1,"feature_code":"api_calls","price":0,"currency":"USD","billing_cycle":"USAGE","pricing_config":{"type":"quota","values":[{"min":0,"max":10000,"price":0},{"min":10001,"max":null,"price":0.001}]}}]}',
  NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 1 MONTH), DATE_ADD(NOW(), INTERVAL 1 MONTH), 'ACTIVE'
);
```

### 2.3 使用量记录示例

```sql
-- 记录API调用使用量
INSERT INTO bill_usage (tenant_id, subscription_id, feature_id, usage_time, usage_amount, unit, billing_period, metadata) VALUES
(1001, 1, 1, NOW(), 1, '次', '2024-01-01', '{"endpoint": "/api/users", "method": "GET", "status": 200}'),
(1001, 1, 1, NOW(), 1, '次', '2024-01-01', '{"endpoint": "/api/orders", "method": "POST", "status": 201}'),
(1002, 2, 1, NOW(), 1, '次', '2024-01-01', '{"endpoint": "/api/products", "method": "GET", "status": 200}');

-- 记录存储使用量
INSERT INTO bill_usage (tenant_id, subscription_id, feature_id, usage_time, usage_amount, unit, billing_period, metadata) VALUES
(1001, 1, 2, NOW(), 0.5, 'GB', '2024-01-01', '{"file_type": "image", "file_size": 524288000}'),
(1002, 2, 2, NOW(), 2.1, 'GB', '2024-01-01', '{"file_type": "document", "file_size": 2147483648}');
```

### 2.4 常用查询示例

```sql
-- 查询订阅的完整定价信息（包含功能定价）
SELECT 
  s.sub_no,
  s.pricing_snapshot,
  s.status,
  JSON_EXTRACT(s.pricing_snapshot, '$.plan_pricing.price') as base_price,
  JSON_EXTRACT(s.pricing_snapshot, '$.plan_pricing.currency') as currency,
  JSON_EXTRACT(s.pricing_snapshot, '$.plan_pricing.billing_cycle') as billing_cycle
FROM bill_subscription s
WHERE s.tenant_id = 1001 AND s.status = 'ACTIVE';

-- 查询租户当月使用量统计
SELECT 
  f.name as feature_name,
  f.unit,
  SUM(u.usage_amount) as total_usage,
  COUNT(*) as usage_count
FROM bill_usage u
JOIN bill_feature f ON u.feature_id = f.id
WHERE u.tenant_id = 1001 
  AND u.billing_period = '2024-01-01'
GROUP BY f.id, f.name, f.unit;

-- 查询订阅的功能配额和当前使用量
SELECT 
  f.name as feature_name,
  pf.feature_value as quota_limit,
  COALESCE(SUM(u.usage_amount), 0) as current_usage,
  f.unit
FROM bill_subscription s
JOIN bill_plan_feature pf ON s.plan_id = pf.plan_id
JOIN bill_feature f ON pf.feature_id = f.id
LEFT JOIN bill_usage u ON s.id = u.subscription_id 
  AND f.id = u.feature_id 
  AND u.billing_period = DATE_FORMAT(NOW(), '%Y-%m-01')
WHERE s.tenant_id = 1001 AND s.status = 'ACTIVE'
GROUP BY f.id, f.name, pf.feature_value, f.unit;

-- 查询需要计费的订阅
SELECT 
  s.id,
  s.sub_no,
  s.tenant_id,
  s.next_billing_time,
  JSON_EXTRACT(s.pricing_snapshot, '$.plan_pricing.price') as amount
FROM bill_subscription s
WHERE s.status = 'ACTIVE'
  AND s.auto_renew = 1
  AND s.next_billing_time <= NOW()
  AND s.next_billing_time IS NOT NULL;

-- 查询超量使用的功能
SELECT 
  s.sub_no,
  f.name as feature_name,
  pf.feature_value as quota_limit,
  SUM(u.usage_amount) as total_usage,
  (SUM(u.usage_amount) - CAST(pf.feature_value AS DECIMAL)) as overage
FROM bill_subscription s
JOIN bill_plan_feature pf ON s.plan_id = pf.plan_id
JOIN bill_feature f ON pf.feature_id = f.id
JOIN bill_usage u ON s.id = u.subscription_id AND f.id = u.feature_id
WHERE s.status = 'ACTIVE'
  AND f.type = 'USAGE'
  AND u.billing_period = DATE_FORMAT(NOW(), '%Y-%m-01')
GROUP BY s.id, s.sub_no, f.id, f.name, pf.feature_value
HAVING total_usage > CAST(pf.feature_value AS DECIMAL);
```

## 3. 设计总结

### 3.1 核心特性

1. **完整的计费流程**：
    - 功能定义 → 套餐配置 → 定价设置 → 订阅管理 → 使用量追踪 → 账单生成 → 支付处理

2. **灵活的定价模式**：
    - 固定费用（月费/年费）
    - 按量计费（quota/tiered/usage/package/tiered_fixed）
    - 混合计费（基础费用 + 超量费用）

3. **多维度支持**：
    - 多席位订阅（quantity字段）
    - 多币种定价（currency字段）
    - 租户专属定价（tenant_id字段）
    - 功能范围控制（PER_SUBSCRIPTION/PER_SEAT）

4. **数据一致性保障**：
    - pricing_snapshot 价格快照机制
    - 试用信息独立管理
    - 避免数据冗余和不一致

### 3.2 性能优化

1. **索引设计**：
    - 复合索引优化查询性能
    - 支持多维度数据检索
    - 计费相关查询优化

2. **数据分区**：
    - bill_usage 表按月分区
    - 历史数据归档策略
    - 大数据量处理优化

3. **查询优化**：
    - JSON 字段查询优化
    - 批量操作支持
    - 异步处理建议

### 3.3 业务场景覆盖

1. **订阅管理**：
    - 试用转付费
    - 套餐升级降级
    - 优雅取消机制

2. **计费处理**：
    - 自动续费
    - 超量计费
    - 按比例计费

3. **账单管理**：
    - 多类型账单
    - 明细追踪
    - 支付状态管理

### 3.4 扩展性设计

1. **版本管理**：
    - 套餐版本并存
    - 平滑升级迁移
    - 历史兼容性

2. **多租户支持**：
    - 数据隔离
    - 专属定价
    - 灵活配置

3. **国际化支持**：
    - 多币种
    - 本地化定价
    - 区域化配置

这个设计能够满足大多数SaaS产品的计费需求，具有良好的扩展性和维护性。