package io.github.rose.core.model;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 具有额外信息的接口
 * 简化版本，只定义基本的 getter/setter
 *
 * @author rose
 */
public interface HasExtra {

    /**
     * 获取额外信息
     */
    JsonNode getExtra();

    /**
     * 设置额外信息
     */
    void setExtra(JsonNode extra);
}
