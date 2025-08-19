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
package io.github.rosestack.core.util;

import static org.apache.commons.lang3.ArrayUtils.EMPTY_CLASS_ARRAY;

import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Proxies is a collection of useful dynamic proxies. Internal use only.
 *
 * @author vladimir
 * @since 4.0 infinispan
 */
public abstract class Proxies {
    private static final Logger log = LoggerFactory.getLogger(Proxies.class);

    public static Object newCatchThrowableProxy(Object obj) {
        return java.lang.reflect.Proxy.newProxyInstance(
                obj.getClass().getClassLoader(), getInterfaces(obj.getClass()), new CatchThrowableProxy(obj));
    }

    private static Class<?>[] getInterfaces(Class<?> clazz) {
        Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces.length > 0) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null && superClass.getInterfaces().length > 0) {
                Class<?>[] superInterfaces = superClass.getInterfaces();
                Class<?>[] clazzes = new Class[interfaces.length + superInterfaces.length];
                System.arraycopy(interfaces, 0, clazzes, 0, interfaces.length);
                System.arraycopy(superInterfaces, 0, clazzes, interfaces.length, superInterfaces.length);
                return clazzes;
            } else {
                return interfaces;
            }
        }
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != Object.class) return superclass.getInterfaces();
        return EMPTY_CLASS_ARRAY;
    }

    /**
     * CatchThrowableProxy is a wrapper around interface that does not allow any exception to be
     * thrown when invoking methods on that interface. All exceptions are logged but not propagated
     * to the caller.
     */
    static class CatchThrowableProxy implements java.lang.reflect.InvocationHandler {

        private final Object obj;

        private CatchThrowableProxy(Object obj) {
            this.obj = obj;
        }

        public static Object newInstance(Object obj) {
            return java.lang.reflect.Proxy.newProxyInstance(
                    obj.getClass().getClassLoader(), obj.getClass().getInterfaces(), new CatchThrowableProxy(obj));
        }

        @Override
        public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
            Object result = null;
            try {
                result = m.invoke(obj, args);
            } catch (Throwable t) {
                log.warn("method {} invoke error: {}, cause: {}", m.getName(), t.getMessage(), t.getCause());
            }
            return result;
        }
    }
}
