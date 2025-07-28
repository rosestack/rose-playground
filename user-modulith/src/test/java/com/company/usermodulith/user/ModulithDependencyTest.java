package com.company.usermodulith.user;

import com.company.usermodulith.UserModulithApplication;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * Spring Modulith 模块依赖测试
 * <p>
 * 验证模块间的依赖关系是否合法
 * </p>
 *
 * @author Chen Soul
 * @since 1.0.0
 */
class ModulithDependencyTest {

    /**
     * 验证模块依赖关系
     */
    @Test
    void verifyModuleDependencies() {
        // 创建应用模块分析器
        ApplicationModules modules = ApplicationModules.of(UserModulithApplication.class);
        
        // 验证模块依赖
        modules.verify();
        
        // 输出模块信息
        System.out.println("=== 模块依赖验证通过 ===");
        System.out.println("模块数量: " + modules.stream().count());
        
        modules.stream().forEach(module -> {
            System.out.println("模块: " + module.getDisplayName());
            System.out.println("  包: " + module.getBasePackage());
            System.out.println();
        });
    }
} 