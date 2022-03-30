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
package org.inspirenxe.timings.core.history.record;

import com.google.common.collect.Maps;
import org.inspirenxe.timings.core.history.Counter;
import org.inspirenxe.timings.core.util.LoadingMap;
import org.inspirenxe.timings.core.util.MRUMapCache;
import org.spongepowered.api.block.entity.BlockEntityType;
import org.spongepowered.api.entity.EntityType;

import java.util.Map;
import java.util.function.Function;

public final class RegionRecord {

    static Function<RegionId, RegionRecord> LOADER = RegionRecord::new;
    private final RegionId id;
    private final Map<BlockEntityType, Counter> blockEntityCounts;
    private final Map<EntityType<?>, Counter> entityCounts;

    RegionRecord(final RegionId id) {
        this.id = id;
        this.blockEntityCounts = MRUMapCache.of(LoadingMap.of(Maps.newHashMap(), Counter.loader()));
        this.entityCounts = MRUMapCache.of(LoadingMap.of(Maps.newHashMap(), Counter.loader()));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        final RegionRecord that = (RegionRecord) o;

        return this.id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    static class RegionId {
        final int x, z;
        final long id;

        RegionId(final int x, final int z) {
            this.x = x >> 5 << 5;
            this.z = z >> 5 << 5;
            this.id = ((long) (this.x) << 32) + (this.z >> 5 << 5) - Integer.MIN_VALUE;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || this.getClass() != o.getClass()) return false;

            final RegionId that = (RegionId) o;

            return this.id == that.id;

        }

        @Override
        public int hashCode() {
            return (int) (this.id ^ (this.id >>> 32));
        }
    }
}
