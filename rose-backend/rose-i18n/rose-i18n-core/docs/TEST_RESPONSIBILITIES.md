# 测试类职责划分

本文档说明了Rose I18n插值系统测试类的职责划分，确保测试职责单一，各司其职，避免重复测试。

## 测试架构原则

1. **单一职责原则** - 每个测试类只负责测试一个特定的方面
2. **避免重复** - 相同的功能只在一个地方测试
3. **清晰边界** - 测试类之间有明确的职责边界
4. **易于维护** - 当功能变更时，只需要修改对应的测试类

## 重构后的测试类职责划分

### 1. SimpleExpressionEvaluatorTest
**职责：专门测试SimpleExpressionEvaluator的具体实现细节和功能**

- ✅ 基本属性访问（user.name, user.age）
- ✅ 方法调用（user.getName(), text.length()）
- ✅ 嵌套属性访问（user.profile.email）
- ✅ 集合操作（list.size(), map.isEmpty()）
- ✅ null值处理和错误处理
- ✅ 空值比较（user.name != null）
- ✅ 支持的表达式类型验证
- ✅ 无效表达式处理
- ✅ 各种数据类型处理
- ✅ 复杂对象图访问

### 2. DefaultMessageInterpolatorTest（合并后的完整测试）
**职责：测试DefaultMessageInterpolator的所有功能，包括核心功能、边界情况和业务场景**

#### 核心插值功能：
- ✅ MessageFormat风格（{0}, {1}, {2}）
- ✅ 命名参数风格（{name}, {age}）
- ✅ 表达式风格（${user.name}）
- ✅ 带Locale的格式化
- ✅ 混合风格处理
- ✅ 自定义表达式评估器

#### 边界情况测试：
- ✅ null和空值处理
- ✅ 格式错误的占位符处理
- ✅ 特殊字符和Unicode处理
- ✅ 性能测试（大模板）
- ✅ 并发安全性测试

#### 业务场景测试：
- ✅ 真实世界用户场景（用户欢迎、订单状态）
- ✅ 邮件模板场景
- ✅ 多语言支持
- ✅ 错误恢复能力

## 重构决策

### 为什么合并测试类？

1. **消除重复**：`InterpolationBoundaryTest`和`InterpolationIntegrationTest`中的测试实际上都是在测试`DefaultMessageInterpolator`的功能
2. **简化维护**：所有相关测试集中在一个文件中，更容易维护和理解
3. **职责清晰**：一个类的测试应该在一个测试文件中
4. **减少复杂性**：避免了测试类之间的职责边界模糊问题

### 删除的测试类：
- ❌ `InterpolationBoundaryTest` - 合并到`DefaultMessageInterpolatorTest`
- ❌ `InterpolationIntegrationTest` - 合并到`DefaultMessageInterpolatorTest`

## 最终测试结构

```
src/test/java/io/github/rose/i18n/interpolation/
├── SimpleExpressionEvaluatorTest.java          # SimpleExpressionEvaluator专项测试
└── DefaultMessageInterpolatorTest.java         # DefaultMessageInterpolator完整测试
```

## 测试运行统计

重构后的测试统计：
- **SimpleExpressionEvaluatorTest**: 14个测试方法
- **DefaultMessageInterpolatorTest**: 20个测试方法（包含核心功能、边界情况、业务场景）

**总计**: 34个测试方法，覆盖所有功能点，无重复测试

## 维护指南

### 添加新测试时的决策树

1. **是否测试SimpleExpressionEvaluator的具体实现？**
   - 是 → 添加到 `SimpleExpressionEvaluatorTest`

2. **是否测试DefaultMessageInterpolator的核心插值功能？**
   - 是 → 添加到 `DefaultMessageInterpolatorTest`

3. **是否测试真实业务场景的集成？**
   - 是 → 添加到 `InterpolationIntegrationTest`

4. **是否测试边界情况、性能或异常处理？**
   - 是 → 添加到 `InterpolationBoundaryTest`

### 修改现有测试时的原则

1. **功能变更** - 只修改对应职责的测试类
2. **重构代码** - 确保测试仍然覆盖相同的职责范围
3. **删除功能** - 删除对应的测试，检查是否有其他测试依赖

## 测试覆盖率目标

- **行覆盖率**: > 90%
- **分支覆盖率**: > 85%
- **方法覆盖率**: > 95%

每个测试类都应该专注于其职责范围内的完整覆盖，而不是追求整体覆盖率。
