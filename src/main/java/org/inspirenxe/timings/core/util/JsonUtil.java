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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

public final class JsonUtil {
    static final Gson gson = new GsonBuilder().serializeNulls().create();

    private JsonUtil() {
    }

    public static JsonArray arrayOf(final Object... elements) {
        return JsonUtil.gson.toJsonTree(elements).getAsJsonArray();
    }

    public static JsonObjectBuilder objectBuilder() {
        return new JsonObjectBuilder();
    }

    public static JsonObject singleObjectPair(final String key, final Object value) {
        return JsonUtil.objectBuilder().add(key, value).build();
    }

    public static JsonObject singleObjectPair(final int key, final Object value) {
        return JsonUtil.objectBuilder().add(key, value).build();
    }

    public static class JsonObjectBuilder {

        private final Map<String, Object> elements = Maps.newHashMap();

        public JsonObjectBuilder add(final int key, final Object value) {
            return this.add(String.valueOf(key), value);
        }

        public JsonObjectBuilder add(final String key, Object value) {
            if (value instanceof JsonObjectBuilder) {
                value = ((JsonObjectBuilder) value).build();
            }
            this.elements.put(key, value);
            return this;
        }

        public JsonObject build() {
            return JsonUtil.gson.toJsonTree(this.elements).getAsJsonObject();
        }
    }

    public static <E> JsonArray mapArray(final E[] elements, final Function<E, Object> function) {
        return JsonUtil.mapArray(Lists.newArrayList(elements), function);
    }

    public static <E> JsonArray mapArray(final Iterable<E> elements, final Function<E, Object> function) {
        List<Object> list = Lists.newArrayList();
        for (E element : elements) {
            Object transformed = function.apply(element);
            if (transformed != null) {
                list.add(transformed);
            }
        }
        return JsonUtil.gson.toJsonTree(list).getAsJsonArray();
    }

    public static <E> JsonObject mapArrayToObject(final E[] array, final Function<E, JsonObject> function) {
        return JsonUtil.mapArrayToObject(Lists.newArrayList(array), function);
    }

    public static <E> JsonObject mapArrayToObject(final Iterable<E> iterable, final Function<E, JsonObject> function) {
        JsonObjectBuilder builder = JsonUtil.objectBuilder();
        for (E element : iterable) {
            JsonObject obj = function.apply(element);
            if (obj == null) {
                continue;
            }
            for (Entry<String, JsonElement> entry : obj.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }
        return builder.build();
    }

    public static String toString(final JsonElement element) {
        return JsonUtil.gson.toJson(element);
    }

    public static JsonElement toJsonElement(final Object value) {
        return JsonUtil.gson.toJsonTree(value);
    }

}