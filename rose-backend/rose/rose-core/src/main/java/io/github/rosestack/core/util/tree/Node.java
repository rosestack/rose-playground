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

public interface Node<T> extends Comparable<Node<T>>, Serializable {

    T getId();

    Node<T> setId(T var1);

    T getParentId();

    Node<T> setParentId(T var1);

    CharSequence getName();

    Node<T> setName(CharSequence var1);

    Comparable<?> getWeight();

    Node<T> setWeight(Comparable<?> var1);

    default int compareTo(Node<T> node) {
        Comparable<?> weight = this.getWeight();
        if (null != weight) {
            Comparable<?> weightOther = node.getWeight();
            return compareWeights(weight, weightOther);
        } else {
            return 0;
        }
    }

    @SuppressWarnings("unchecked")
    static int compareWeights(Comparable<?> weight1, Comparable<?> weight2) {
        return ((Comparable<Object>) weight1).compareTo(weight2);
    }
}
