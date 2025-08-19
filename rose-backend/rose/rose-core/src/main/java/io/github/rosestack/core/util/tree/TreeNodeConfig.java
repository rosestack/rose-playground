/*
 * Copyright © 2025 rosestack.github.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.rosestack.core.util.tree;

import java.io.Serializable;

/**
 * 树配置属性相关
 *
 * @author liangbaikai
 */
public class TreeNodeConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 默认属性配置对象
     */
    public static TreeNodeConfig DEFAULT_CONFIG = new TreeNodeConfig();

    // 属性名配置字段
    private String idKey = "id";

    private String parentIdKey = "parentId";

    private String weightKey = "weight";

    private String nameKey = "name";

    private String childrenKey = "children";

    private Boolean reversed = false;

    // 可以配置递归深度 从0开始计算 默认此配置为空,即不限制
    private Integer deep;

    public TreeNodeConfig() {}

    public String getIdKey() {
        return idKey;
    }

    public void setIdKey(String idKey) {
        this.idKey = idKey;
    }

    public String getParentIdKey() {
        return parentIdKey;
    }

    public void setParentIdKey(String parentIdKey) {
        this.parentIdKey = parentIdKey;
    }

    public String getWeightKey() {
        return weightKey;
    }

    public void setWeightKey(String weightKey) {
        this.weightKey = weightKey;
    }

    public String getNameKey() {
        return nameKey;
    }

    public void setNameKey(String nameKey) {
        this.nameKey = nameKey;
    }

    public String getChildrenKey() {
        return childrenKey;
    }

    public void setChildrenKey(String childrenKey) {
        this.childrenKey = childrenKey;
    }

    public Boolean getReversed() {
        return reversed;
    }

    public void setReversed(Boolean reversed) {
        this.reversed = reversed;
    }

    public Integer getDeep() {
        return deep;
    }

    public void setDeep(Integer deep) {
        this.deep = deep;
    }
}
