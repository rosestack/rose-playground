package io.github.rosestack.infrastructure.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 自定义元数据处理器
 * <p>
 * 用于自动填充实体类中的创建时间、更新时间等字段，减少重复代码
 * 需要在实体类的字段上添加@TableField注解，并设置fill属性
 * 例如：@TableField(fill = FieldFill.INSERT) 或 @TableField(fill = FieldFill.INSERT_UPDATE)
 */
@Slf4j
@Component
public class CustomMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("开始执行插入填充...");
        String currentUsername = getCurrentUsername();

        this.strictInsertFill(metaObject, "createdTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "createdBy", String.class, currentUsername);

        this.strictInsertFill(metaObject, "updatedTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updatedBy", String.class, currentUsername);

        this.strictInsertFill(metaObject, "deleted", Boolean.class, false);
        this.strictInsertFill(metaObject, "version", Integer.class, 1);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("开始执行更新填充...");
        String currentUsername = getCurrentUsername();
        this.strictUpdateFill(metaObject, "updatedTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updatedBy", String.class, currentUsername);

    }

    private String getCurrentUsername() {
        try {
            // todo 从Spring Security上下文获取
            return "system";
        } catch (Exception e) {
            log.warn("获取当前用户名失败", e);
            return "system";
        }
    }
}