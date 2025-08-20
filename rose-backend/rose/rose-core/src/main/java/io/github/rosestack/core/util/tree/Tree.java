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

import java.util.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;

/**
 * 通过转换器将你的实体转化为TreeNodeMap节点实体 属性都存在此处,属性有序，可支持排序
 *
 * @param <T> ID类型
 * @author liangbaikai
 * @since 5.2.1
 */
public class Tree<T> extends LinkedHashMap<String, Object> implements Node<T> {

    private static final long serialVersionUID = 1L;

    private final TreeNodeConfig treeNodeConfig;

    private Tree<T> parent;

    public Tree() {
        this(null);
    }

    /**
     * 构造
     *
     * @param treeNodeConfig TreeNode配置
     */
    public Tree(TreeNodeConfig treeNodeConfig) {
        this.treeNodeConfig = ObjectUtils.defaultIfNull(treeNodeConfig, TreeNodeConfig.DEFAULT_CONFIG);
    }

    /**
     * 获取节点配置
     *
     * @return 节点配置
     * @since 5.7.2
     */
    public TreeNodeConfig getConfig() {
        return this.treeNodeConfig;
    }

    /**
     * 获取父节点
     *
     * @return 父节点
     * @since 5.2.4
     */
    public Tree<T> getParent() {
        return parent;
    }

    /**
     * 设置父节点
     *
     * @param parent 父节点
     * @return this
     * @since 5.2.4
     */
    public Tree<T> setParent(Tree<T> parent) {
        this.parent = parent;
        if (null != parent) {
            this.setParentId(parent.getId());
        }
        return this;
    }

    /**
     * 获取ID对应的节点，如果有多个ID相同的节点，只返回第一个。<br>
     * 此方法只查找此节点及子节点，采用广度优先遍历。
     *
     * @param id ID
     * @return 节点
     * @since 5.2.4
     */
    public Tree<T> getNode(T id) {
        return TreeUtil.getNode(this, id);
    }

    /**
     * 获取所有父节点名称列表
     *
     * <p>
     * 比如有个人在研发1部，他上面有研发部，接着上面有技术中心<br>
     * 返回结果就是：[研发一部, 研发中心, 技术中心]
     *
     * @param id                 节点ID
     * @param includeCurrentNode 是否包含当前节点的名称
     * @return 所有父节点名称列表
     * @since 5.2.4
     */
    public List<CharSequence> getParentsName(T id, boolean includeCurrentNode) {
        return TreeUtil.getParentsName(getNode(id), includeCurrentNode);
    }

    /**
     * 获取所有父节点名称列表
     *
     * <p>
     * 比如有个人在研发1部，他上面有研发部，接着上面有技术中心<br>
     * 返回结果就是：[研发一部, 研发中心, 技术中心]
     *
     * @param includeCurrentNode 是否包含当前节点的名称
     * @return 所有父节点名称列表
     * @since 5.2.4
     */
    public List<CharSequence> getParentsName(boolean includeCurrentNode) {
        return TreeUtil.getParentsName(this, includeCurrentNode);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getId() {
        return (T) this.get(treeNodeConfig.getIdKey());
    }

    @Override
    public Tree<T> setId(T id) {
        this.put(treeNodeConfig.getIdKey(), id);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getParentId() {
        return (T) this.get(treeNodeConfig.getParentIdKey());
    }

    @Override
    public Tree<T> setParentId(T parentId) {
        this.put(treeNodeConfig.getParentIdKey(), parentId);
        return this;
    }

    @Override
    public CharSequence getName() {
        return (CharSequence) this.get(treeNodeConfig.getNameKey());
    }

    @Override
    public Tree<T> setName(CharSequence name) {
        this.put(treeNodeConfig.getNameKey(), name);
        return this;
    }

    @Override
    public Comparable<?> getWeight() {
        return (Comparable<?>) this.get(treeNodeConfig.getWeightKey());
    }

    @Override
    public Tree<T> setWeight(Comparable<?> weight) {
        this.put(treeNodeConfig.getWeightKey(), weight);
        return this;
    }

    /**
     * 获取所有子节点
     *
     * @return 所有子节点
     */
    @SuppressWarnings("unchecked")
    public List<Tree<T>> getChildren() {
        return (List<Tree<T>>) this.getOrDefault(treeNodeConfig.getChildrenKey(), new LinkedList<>());
    }

    /**
     * 设置子节点，设置后会覆盖所有原有子节点
     *
     * @param children 子节点列表
     * @return this
     */
    public Tree<T> setChildren(List<Tree<T>> children) {
        this.put(treeNodeConfig.getChildrenKey(), children);
        return this;
    }

    /**
     * 增加子节点，同时关联子节点的父节点为当前节点
     *
     * @param children 子节点列表
     * @return this
     * @since 5.6.7
     */
    @SafeVarargs
    public final Tree<T> addChildren(Tree<T>... children) {
        if (ArrayUtils.isNotEmpty(children)) {
            List<Tree<T>> childrenList = this.getChildren();
            if (ObjectUtils.isEmpty(childrenList)) {
                setChildren(childrenList);
            }
            for (Tree<T> child : children) {
                child.setParent(this);
                childrenList.add(child);
            }
            // 每次添加一个记录，就排序一下
            Collections.sort(childrenList);
        }
        return this;
    }

    /**
     * 扩展属性
     *
     * @param key   键
     * @param value 扩展值
     */
    public void putExtra(String key, Object value) {
        if (key.length() == 0) {
            throw new RuntimeException("Key must be not empty !");
        }
        this.put(key, value);
    }

    @Override
    public int compareTo(Node<T> node) {
        Comparable<?> weight = this.getWeight();
        if (null != weight) {
            Comparable<?> weightOther = node.getWeight();
            return treeNodeConfig.getReversed()
                ? Node.compareWeights(weightOther, weight)
                : Node.compareWeights(weight, weightOther);
        } else {
            return 0;
        }
    }
}
