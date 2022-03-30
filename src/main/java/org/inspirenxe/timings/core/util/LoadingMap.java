/*
 * This file is part of timings-core, licensed under the MIT License (MIT).
 *
 * Copyright (c) InspireNXE <https://inspirenxe.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.inspirenxe.timings.core.util;

import java.lang.reflect.Constructor;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Allows you to pass a Loader function that when a key is accessed that doesn't
 * exist, automatically loads the entry into the map by calling the loader
 * Function.
 * <p>
 * .get() Will only return null if the Loader can return null.
 * <p>
 * You may pass any backing Map to use.
 * <p>
 * This class is not thread safe and should be wrapped with
 * Collections.synchronizedMap on the OUTSIDE of the LoadingMap if needed.
 * <p>
 * Do not wrap the backing map with Collections.synchronizedMap.
 *
 * @param <K> Key
 * @param <V> Value
 */
public final class LoadingMap<K, V> extends AbstractMap<K, V> {

    private final Map<K, V> backingMap;
    private final Function<K, V> loader;

    /**
     * Initializes an auto loading map using specified loader and backing map
     *
     * @param backingMap Map to wrap
     * @param loader Loader
     */
    public LoadingMap(final Map<K, V> backingMap, final Function<K, V> loader) {
        this.backingMap = backingMap;
        this.loader = loader;
    }

    /**
     * Creates a new LoadingMap with the specified map and loader
     *
     * @param backingMap Actual map being used.
     * @param loader Loader to use
     * @param <K> Key Type of the Map
     * @param <V> Value Type of the Map
     * @return Map
     */
    public static <K, V> Map<K, V> of(final Map<K, V> backingMap, final Function<K, V> loader) {
        return new LoadingMap<>(backingMap, loader);
    }

    /**
     * Creates a LoadingMap with an auto instantiating loader.
     * <p>
     * Will auto construct class of of Value when not found
     * <p>
     * Since this uses Reflection, It is more effecient to define your own
     * static loader than using this helper, but if performance is not critical,
     * this is easier.
     *
     * @param backingMap Actual map being used.
     * @param keyClass Class used for the K generic
     * @param valueClass Class used for the V generic
     * @param <K> Key Type of the Map
     * @param <V> Value Type of the Map
     * @return Map that auto instantiates on .get()
     */
    public static <K, V> Map<K, V> newAutoMap(final Map<K, V> backingMap, final Class<? extends K> keyClass,
            final Class<? extends V> valueClass) {
        return new LoadingMap<>(backingMap, new AutoInstantiatingLoader<>(keyClass, valueClass));
    }

    /**
     * Creates a LoadingMap with an auto instantiating loader.
     * <p>
     * Will auto construct class of Value when not found
     * <p>
     * Since this uses Reflection, It is more efficient to define your own
     * static loader than using this helper, but if performance is not critical,
     * this is easier.
     *
     * @param backingMap Actual map being used.
     * @param valueClass Class used for the V generic
     * @param <K> Key Type of the Map
     * @param <V> Value Type of the Map
     * @return Map that auto instantiates on .get()
     */
    public static <K, V> Map<K, V> newAutoMap(final Map<K, V> backingMap,
            final Class<? extends V> valueClass) {
        return LoadingMap.newAutoMap(backingMap, null, valueClass);
    }

    /**
     * @see #newAutoMap
     *
     * new Auto initializing map using a HashMap.
     *
     * @param keyClass Class used for the K generic
     * @param valueClass Class used for the V generic
     * @param <K> Key Type of the Map
     * @param <V> Value Type of the Map
     * @return Map that auto instantiates on .get()
     */
    public static <K, V> Map<K, V> newHashAutoMap(final Class<? extends K> keyClass, final Class<? extends V> valueClass) {
        return LoadingMap.newAutoMap(new HashMap<>(), keyClass, valueClass);
    }

    /**
     * @see #newAutoMap
     *
     * new Auto initializing map using a HashMap.
     *
     * @param valueClass Class used for the V generic
     * @param <K> Key Type of the Map
     * @param <V> Value Type of the Map
     * @return Map that auto instantiates on .get()
     */
    public static <K, V> Map<K, V> newHashAutoMap(final Class<? extends V> valueClass) {
        return LoadingMap.newHashAutoMap(null, valueClass);
    }

    /**
     * @see #newAutoMap
     *
     * new Auto initializing map using a HashMap.
     *
     * @param keyClass Class used for the K generic
     * @param valueClass Class used for the V generic
     * @param initialCapacity Initial capacity to use
     * @param loadFactor Load factor to use
     * @param <K> Key Type of the Map
     * @param <V> Value Type of the Map
     * @return Map that auto instantiates on .get()
     */
    public static <K, V> Map<K, V> newHashAutoMap(final Class<? extends K> keyClass, final Class<? extends V> valueClass, final int initialCapacity,
            final float loadFactor) {
        return LoadingMap.newAutoMap(new HashMap<>(initialCapacity, loadFactor), keyClass, valueClass);
    }

    /**
     * @see #newAutoMap
     *
     * new Auto initializing map using a HashMap.
     *
     * @param valueClass Class used for the V generic
     * @param initialCapacity Initial capacity to use
     * @param loadFactor Load factor to use
     * @param <K> Key Type of the Map
     * @param <V> Value Type of the Map
     * @return  Map that auto instantiates on .get()
     */
    public static <K, V> Map<K, V> newHashAutoMap(final Class<? extends V> valueClass, final int initialCapacity, final float loadFactor) {
        return LoadingMap.newHashAutoMap(null, valueClass, initialCapacity, loadFactor);
    }

    /**
     * Initializes an auto loading map using a HashMap
     *
     * @param loader Loader to use
     * @param <K> Key Type of the Map
     * @param <V> Value Type of the Map
     * @return Map
     */
    public static <K, V> Map<K, V> newHashMap(final Function<K, V> loader) {
        return new LoadingMap<>(new HashMap<>(), loader);
    }

    /**
     * Initializes an auto loading map using a HashMap
     *
     * @param loader Loader to use
     * @param initialCapacity Initial capacity to use
     * @param loadFactor Load factor to use
     * @param <K> Key Type of the Map
     * @param <V> Value Type of the Map
     * @return Map
     */
    public static <K, V> Map<K, V> newHashMap(final Function<K, V> loader, final int initialCapacity, final float loadFactor) {
        return new LoadingMap<>(new HashMap<>(initialCapacity, loadFactor), loader);
    }

    /**
     * Initializes an auto loading map using an Identity HashMap
     *
     * @param loader Loader to use
     * @param <K> Key Type of the Map
     * @param <V> Value Type of the Map
     * @return Map
     */
    public static <K, V> Map<K, V> newIdentityHashMap(final Function<K, V> loader) {
        return new LoadingMap<>(new IdentityHashMap<>(), loader);
    }

    /**
     * Initializes an auto loading map using an Identity HashMap
     *
     * @param loader Loader to use
     * @param initialCapacity Initial capacity to use
     * @param <K> Key Type of the Map
     * @param <V> Value Type of the Map
     * @return Map
     */
    public static <K, V> Map<K, V> newIdentityHashMap(final Function<K, V> loader, final int initialCapacity) {
        return new LoadingMap<>(new IdentityHashMap<>(initialCapacity), loader);
    }

    @Override
    public int size() {
        return this.backingMap.size();
    }

    @Override
    public boolean isEmpty() {
        return this.backingMap.isEmpty();
    }

    @Override
    public boolean containsKey(final Object key) {
        return this.backingMap.containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        return this.backingMap.containsValue(value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(final Object key) {
        V res = this.backingMap.get(key);
        if (res == null && key != null) {
            res = this.loader.apply((K) key);
            if (res != null) {
                this.backingMap.put((K) key, res);
            }
        }
        return res;
    }

    @Override
    public V put(final K key, final V value) {
        return this.backingMap.put(key, value);
    }

    @Override
    public V remove(final Object key) {
        return this.backingMap.remove(key);
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        this.backingMap.putAll(m);
    }

    @Override
    public void clear() {
        this.backingMap.clear();
    }

    @Override
    public Set<K> keySet() {
        return this.backingMap.keySet();
    }

    @Override
    public Collection<V> values() {
        return this.backingMap.values();
    }

    @Override
    public boolean equals(final Object o) {
        return this.backingMap.equals(o);
    }

    @Override
    public int hashCode() {
        return this.backingMap.hashCode();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return this.backingMap.entrySet();
    }

    @Override
    public LoadingMap<K, V> clone() {
        return new LoadingMap<>(this.backingMap, this.loader);
    }

    private static class AutoInstantiatingLoader<K, V> implements Function<K, V> {

        final Constructor<? extends V> constructor;
        private final Class<? extends V> valueClass;

        AutoInstantiatingLoader(final Class<? extends K> keyClass, final Class<? extends V> valueClass) {
            try {
                this.valueClass = valueClass;
                if (keyClass != null) {
                    this.constructor = valueClass.getConstructor(keyClass);
                } else {
                    this.constructor = null;
                }
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(valueClass.getName() + " does not have a constructor for " + keyClass.getName());
            }
        }

        @Override
        public V apply(final K input) {
            try {
                return (this.constructor != null ? this.constructor.newInstance(input) : this.valueClass.newInstance());
            } catch (Exception e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public boolean equals(Object object) {
            return false;
        }
    }

    /**
     * Due to java stuff, you will need to cast it to (Function) for some cases
     *
     * @param <T> Type
     */
    public static abstract class Feeder<T> implements Function<T, T> {

        @Override
        public @Nullable T apply(final @Nullable Object input) {
            return this.apply();
        }

        public abstract T apply();

    }
}