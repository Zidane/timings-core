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
package org.inspirenxe.timings.core.type;

import co.aikar.timings.Timing;
import net.minecraft.world.level.Level;
import org.inspirenxe.timings.core.VanillaTimingsEngine;
import org.inspirenxe.timings.core.duck.EngineDuck;
import org.spongepowered.api.world.World;

public final class LevelTimings {

    public final Timing scheduledBlocks;
    public final Timing entityRemoval;
    public final Timing blockEntityTick;
    public final Timing blockEntityPending;
    public final Timing blockEntityRemoval;
    public final Timing tick;
    public final Timing tickEntities;

    public LevelTimings(final Level level) {
        final VanillaTimingsEngine timingsEngine = ((EngineDuck) ((World) level).engine()).timingsManager();

        final String name = level.dimension().location() + " - ";

        this.tick = timingsEngine.of(name + "Tick");
        this.tickEntities = timingsEngine.of(name + "Tick Entities", this.tick);
        this.scheduledBlocks = timingsEngine.of(name + "Scheduled Blocks", this.tick);

        this.entityRemoval = timingsEngine.of(name + "entityRemoval");
        this.blockEntityTick = timingsEngine.of(name + "blockEntityTick");
        this.blockEntityPending = timingsEngine.of(name + "blockEntityPending");
        this.blockEntityRemoval = timingsEngine.of(name + "blockEntityRemoval");
    }
}
