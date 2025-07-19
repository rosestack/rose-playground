package io.github.rose.core.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 可审计的接口
 * 组合了ID和审计功能
 *
 * @param <ID> ID 类型
 * @author rose
 */
public interface Auditable<ID extends Serializable> extends HasId<ID> {
    
    /**
     * 获取创建时间
     */
    LocalDateTime getCreateTime();
    
    /**
     * 设置创建时间
     */
    void setCreateTime(LocalDateTime createTime);
    
    /**
     * 获取更新时间
     */
    LocalDateTime getUpdateTime();
    
    /**
     * 设置更新时间
     */
    void setUpdateTime(LocalDateTime updateTime);
}
