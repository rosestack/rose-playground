/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.rose.core.collection;


import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * The utilities class for Java {@link List}
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @see List
 * @since 1.0.0
 */
public abstract class ListUtils {
    /**
     * Performs the given action for each element of the specified list, providing both the index and the element.
     *
     * <p>This method iterates over the elements of the list and applies the provided bi-consumer function
     * to each element along with its index. It is useful when operations need to take into account the position
     * of the element in the list.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     List<String> fruits = Arrays.asList("apple", "banana", "cherry");
     *     ListUtils.forEach(fruits, (index, fruit) -> log.info("Index: " + index + ", Fruit: " + fruit));
     *     // Output:
     *     // Index: 0, Fruit: apple
     *     // Index: 1, Fruit: banana
     *     // Index: 2, Fruit: cherry
     *
     *     List<Integer> numbers = Collections.emptyList();
     *     ListUtils.forEach(numbers, (index, number) -> log.info("Index: " + index + ", Number: " + number));
     *     // No output, as the list is empty
     * }</pre>
     *
     * @param values                 the list to iterate over
     * @param indexedElementConsumer the action to perform on each element, taking the index and the element as arguments
     * @param <T>                    the type of elements in the list
     */
    public static <T> void forEach(List<T> values, BiConsumer<Integer, T> indexedElementConsumer) {
        int length = CollectionUtils.size(values);
        for (int i = 0; i < length; i++) {
            T value = values.get(i);
            indexedElementConsumer.accept(i, value);
        }
    }

    /**
     * Performs the given action for each element of the specified list.
     *
     * <p>This method ignores the index of elements and directly applies the provided consumer
     * to each element in the list. It is useful when the operation does not require the element's index.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *     List<String> fruits = Arrays.asList("apple", "banana", "cherry");
     *     ListUtils.forEach(fruits, fruit -> log.info("Fruit: " + fruit));
     *     // Output:
     *     // Fruit: apple
     *     // Fruit: banana
     *     // Fruit: cherry
     *
     *     List<Integer> numbers = Collections.emptyList();
     *     ListUtils.forEach(numbers, number -> log.info("Number: " + number));
     *     // No output, as the list is empty
     * }</pre>
     *
     * @param values   the list to iterate over
     * @param consumer the action to perform on each element
     * @param <T>      the type of elements in the list
     */
    public static <T> void forEach(List<T> values, Consumer<T> consumer) {
        forEach(values, (i, e) -> consumer.accept(e));
    }

    private ListUtils() {
    }
}
