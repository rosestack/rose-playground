package io.github.rose.core.domain;

/**
 * 具有状态的接口
 *
 * @author rose
 */
public interface HasStatus {
    
    /**
     * 获取状态
     */
    String getStatus();
    
    /**
     * 设置状态
     */
    void setStatus(String status);
}
