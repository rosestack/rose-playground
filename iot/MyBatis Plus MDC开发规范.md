## 7. 基础实体设计规范

### 7.1 基础实体架构

#### 7.1.1 实体继承体系
```java
// 基础实体类 - 所有实体的父类
public abstract class BaseEntity {
    // 通用字段：id, createdTime, updatedTime, createdBy, updatedBy, deleted, version
}

// 多租户实体类 - 需要租户隔离的实体
public abstract class TenantEntity extends BaseEntity {
    // 增加租户字段：tenantId
}

// 业务实体类 - 具体的业务实体
public class ProductCategory extends TenantEntity {
    // 业务字段
}
```

#### 7.1.2 实体分类
- **系统级实体**：继承BaseEntity，不包含租户ID，用于系统配置等
- **租户级实体**：继承TenantEntity，包含租户ID，用于业务数据

### 7.2 BaseEntity基础实体类

#### 7.2.1 基础字段定义
```java
/**
 * 基础实体类
 * <p>
 * 包含所有实体通用的基础字段，如ID、创建时间、更新时间、逻辑删除标识等。
 * 所有业务实体都应该继承此类。
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class BaseEntity {

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 创建时间 */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /** 更新时间 */
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /** 创建人 */
    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private String createdBy;

    /** 更新人 */
    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    /** 逻辑删除标识（0-未删除，1-已删除） */
    @TableLogic
    @TableField(value = "deleted")
    private Integer deleted;

    /** 版本号（乐观锁） */
    @Version
    @TableField(value = "version")
    private Integer version;

    /**
     * 获取实体类型名称
     * 用于日志记录和错误信息显示。
     */
    public abstract String getEntityTypeName();

    /**
     * 获取实体业务标识
     * 用于日志记录和错误信息显示，通常是实体的名称或编码。
     */
    public abstract String getEntityIdentifier();

    /**
     * 是否为新增实体
     * 根据ID是否为空判断是否为新增实体。
     */
    public boolean isNew() {
        return this.id == null;
    }

    /**
     * 是否为已删除实体
     * 根据逻辑删除标识判断是否为已删除实体。
     */
    public boolean isDeleted() {
        return this.deleted != null && this.deleted == 1;
    }

    /**
     * 设置删除状态
     * 设置逻辑删除标识。
     */
    public void setDeleted(boolean deleted) {
        this.deleted = deleted ? 1 : 0;
    }
}
```

#### 7.2.2 字段说明

| 字段名 | 类型 | 说明 | 注解 |
|--------|------|------|------|
| id | Long | 主键ID | @TableId(type = IdType.AUTO) |
| createdTime | LocalDateTime | 创建时间 | @TableField(fill = FieldFill.INSERT) |
| updatedTime | LocalDateTime | 更新时间 | @TableField(fill = FieldFill.INSERT_UPDATE) |
| createdBy | String | 创建人 | @TableField(fill = FieldFill.INSERT) |
| updatedBy | String | 更新人 | @TableField(fill = FieldFill.INSERT_UPDATE) |
| deleted | Integer | 逻辑删除标识 | @TableLogic |
| version | Integer | 版本号（乐观锁） | @Version |

### 7.3 TenantEntity多租户实体类

#### 7.3.1 多租户实体定义
```java
/**
 * 多租户基础实体类
 * <p>
 * 继承BaseEntity，增加租户ID字段，用于多租户数据隔离。
 * 所有需要多租户支持的实体都应该继承此类。
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class TenantEntity extends BaseEntity {

    /** 租户ID */
    @TableField(value = "tenant_id")
    private Long tenantId;

    /**
     * 是否为系统级实体
     * 根据租户ID是否为空判断是否为系统级实体。
     * 系统级实体不属于任何特定租户，通常用于系统配置等。
     */
    public boolean isSystemEntity() {
        return this.tenantId == null;
    }

    /**
     * 是否为租户级实体
     * 根据租户ID是否不为空判断是否为租户级实体。
     * 租户级实体属于特定租户，需要进行数据隔离。
     */
    public boolean isTenantEntity() {
        return this.tenantId != null;
    }
}
```

#### 7.3.2 多租户设计原则
- **数据隔离**：不同租户的数据完全隔离，互不干扰
- **字段统一**：所有租户级实体都包含tenantId字段
- **查询过滤**：查询时自动添加租户条件
- **权限控制**：基于租户的权限控制

### 7.4 业务实体设计规范

#### 7.4.1 租户级实体示例
```java
/**
 * 产品分类实体
 * <p>
 * 用于管理物联网产品的分类信息，支持多级分类和租户隔离。
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("iot_product_category")
public class ProductCategory extends TenantEntity {

    /** 分类名称 */
    @TableField("name")
    private String name;

    /** 分类标识符 */
    @TableField("code")
    private String code;

    /** 父分类ID */
    @TableField("parent_id")
    private Long parentId;

    /** 分类层级 */
    @TableField("level")
    private Integer level;

    /** 排序 */
    @TableField("sort_order")
    private Integer sortOrder;

    /** 分类图标 */
    @TableField("icon")
    private String icon;

    /** 分类描述 */
    @TableField("description")
    private String description;

    /** 分类类型：STANDARD-标准行业分类，CUSTOM-自定义分类 */
    @TableField("type")
    private String type;

    /** 关联的物模型模板ID */
    @TableField("template_id")
    private Long templateId;

    /** 分类状态：ACTIVE-激活，INACTIVE-未激活 */
    @TableField("status")
    private String status;

    @Override
    public String getEntityTypeName() {
        return "产品分类";
    }

    @Override
    public String getEntityIdentifier() {
        return this.name + "(" + this.code + ")";
    }

    /**
     * 是否为根分类
     */
    public boolean isRoot() {
        return this.parentId == null;
    }

    /**
     * 是否为子分类
     */
    public boolean isChild() {
        return this.parentId != null;
    }

    /**
     * 是否为标准分类
     */
    public boolean isStandard() {
        return "STANDARD".equals(this.type);
    }

    /**
     * 是否为自定义分类
     */
    public boolean isCustom() {
        return "CUSTOM".equals(this.type);
    }

    /**
     * 是否为激活状态
     */
    public boolean isActive() {
        return "ACTIVE".equals(this.status);
    }
}
```

#### 7.4.2 系统级实体示例
```java
/**
 * 系统配置实体
 * <p>
 * 用于存储系统级配置信息，不包含租户隔离。
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("iot_system_config")
public class SystemConfig extends BaseEntity {

    /** 配置键 */
    @TableField("config_key")
    private String configKey;

    /** 配置值 */
    @TableField("config_value")
    private String configValue;

    /** 配置类型：STRING, NUMBER, BOOLEAN, JSON, FILE */
    @TableField("config_type")
    private String configType;

    /** 配置描述 */
    @TableField("description")
    private String description;

    /** 是否系统配置 */
    @TableField("is_system")
    private Boolean isSystem;

    /** 配置状态：ACTIVE, INACTIVE */
    @TableField("status")
    private String status;

    @Override
    public String getEntityTypeName() {
        return "系统配置";
    }

    @Override
    public String getEntityIdentifier() {
        return this.configKey;
    }

    /**
     * 是否为系统配置
     */
    public boolean isSystemConfig() {
        return Boolean.TRUE.equals(this.isSystem);
    }

    /**
     * 是否为激活状态
     */
    public boolean isActive() {
        return "ACTIVE".equals(this.status);
    }
}
```

### 7.5 实体设计最佳实践

#### 7.5.1 继承选择原则
```java
// ✅ 推荐：系统级实体继承BaseEntity
public class SystemConfig extends BaseEntity {
    // 系统配置，不需要租户隔离
}

// ✅ 推荐：租户级实体继承TenantEntity
public class ProductCategory extends TenantEntity {
    // 业务数据，需要租户隔离
}

// ❌ 不推荐：直接继承Object
public class ProductCategory {
    // 缺少通用字段，不便于维护
}
```

#### 7.5.2 字段设计规范
```java
// ✅ 推荐：使用String类型存储枚举值
@TableField("status")
private String status; // "ACTIVE", "INACTIVE"

// ✅ 推荐：提供便捷的判断方法
public boolean isActive() {
    return "ACTIVE".equals(this.status);
}

// ❌ 不推荐：直接使用枚举类型
@TableField("status")
private Status status; // 可能导致序列化问题
```

#### 7.5.3 方法设计规范
```java
// ✅ 推荐：实现抽象方法
@Override
public String getEntityTypeName() {
    return "产品分类";
}

@Override
public String getEntityIdentifier() {
    return this.name + "(" + this.code + ")";
}

// ✅ 推荐：提供业务判断方法
public boolean isRoot() {
    return this.parentId == null;
}

public boolean isActive() {
    return "ACTIVE".equals(this.status);
}
```

#### 7.5.4 注解使用规范
```java
// ✅ 推荐：使用@TableName指定表名
@TableName("iot_product_category")
public class ProductCategory extends TenantEntity {

// ✅ 推荐：使用@TableField指定字段名
@TableField("parent_id")
private Long parentId;

// ✅ 推荐：使用@TableLogic标记逻辑删除字段
@TableLogic
@TableField("deleted")
private Integer deleted;

// ✅ 推荐：使用@Version标记乐观锁字段
@Version
@TableField("version")
private Integer version;
```

### 7.6 实体使用检查清单

在创建实体类时，请确保：

#### 7.6.1 基础检查
- [ ] 继承了正确的基类（BaseEntity或TenantEntity）
- [ ] 实现了抽象方法getEntityTypeName()和getEntityIdentifier()
- [ ] 使用了正确的注解（@TableName、@TableField等）
- [ ] 字段命名符合数据库命名规范

#### 7.6.2 多租户检查
- [ ] 租户级实体继承了TenantEntity
- [ ] 系统级实体继承了BaseEntity
- [ ] 查询时考虑了租户隔离
- [ ] 权限控制基于租户

#### 7.6.3 业务检查
- [ ] 提供了必要的业务判断方法
- [ ] 字段类型选择合理
- [ ] 注释完整清晰
- [ ] 符合业务逻辑要求 