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
package org.inspirenxe.timings.core.export;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.spongepowered.api.block.entity.BlockEntityType;
import org.spongepowered.api.entity.EntityType;

final class TimingIdHelper {

    private static final Object2IntMap<EntityType<?>> ENTITY_IDS = new Object2IntOpenHashMap<>();
    private static final Object2IntMap<BlockEntityType> BLOCK_ENTITY_IDS = new Object2IntOpenHashMap<>();
    private static final int NOT_FOUND = Integer.MIN_VALUE;
    private static int nextEntityId = 56991891; // Some random number
    private static int nextBlockEntityId = 13221456; // Some random number

    public static int idFor(final EntityType<?> type) {
        int fake;
        if ((fake = TimingIdHelper.ENTITY_IDS.getInt(type)) == TimingIdHelper.NOT_FOUND) {
            fake = TimingIdHelper.nextEntityId++;
            TimingIdHelper.ENTITY_IDS.put(type, fake);
        }
        return fake;
    }

    public static int idFor(final BlockEntityType type) {
        int fake;
        if ((fake = TimingIdHelper.BLOCK_ENTITY_IDS.getInt(type)) == TimingIdHelper.NOT_FOUND) {
            fake = TimingIdHelper.nextBlockEntityId++;
            TimingIdHelper.BLOCK_ENTITY_IDS.put(type, fake);
        }
        return fake;
    }
}