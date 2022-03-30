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
package org.inspirenxe.timings.core.history;

import com.google.gson.JsonElement;
import org.inspirenxe.timings.core.AbstractTiming;
import org.inspirenxe.timings.core.VanillaTimingsEngine;
import org.inspirenxe.timings.core.util.JsonUtil;
import org.spongepowered.api.block.entity.BlockEntityType;
import org.spongepowered.api.entity.EntityType;

import java.util.HashSet;
import java.util.Set;

public final class HistoryRecord {

    public final VanillaTimingsEngine manager;
    public final long endTime;
    public final long startTime;
    public final long totalTicks;
    public final long totalTime;
    public final MinuteReport[] minuteReports;
    public final TimingHistoryEntry[] entries;

    public final Set<EntityType<?>> entityTypes;
    public final Set<BlockEntityType> blockEntityTypes;
    public HistoryRecord(final VanillaTimingsEngine manager) {
        this.manager = manager;
        this.endTime = System.currentTimeMillis() / 1000;
        this.startTime = this.manager.ticksTracker.historyStart / 1000;
        if (this.manager.ticksTracker.timedTicks % 1200 != 0 || this.manager.timingsReportsPerMinute.isEmpty()) {
            this.minuteReports = this.manager.timingsReportsPerMinute.toArray(new MinuteReport[this.manager.timingsReportsPerMinute.size() + 1]);
            this.minuteReports[this.minuteReports.length - 1] = new MinuteReport(manager);
        } else {
            this.minuteReports = this.manager.timingsReportsPerMinute.toArray(new MinuteReport[0]);
        }
        long ticks = 0;
        for (final MinuteReport mp : this.minuteReports) {
            ticks += mp.ticksRecord.timed;
        }
        this.totalTicks = ticks;
        this.totalTime = this.manager.engineTickTiming.data.totalTime;
        this.entries = new TimingHistoryEntry[this.manager.timings.size()];

        int i = 0;
        for (final AbstractTiming timing : this.manager.timings) {
            this.entries[i++] = new TimingHistoryEntry(timing);
        }

        this.entityTypes = new HashSet<>();
        this.blockEntityTypes = new HashSet<>();
    }

    public JsonElement asJson() {
        return JsonUtil.objectBuilder()
                .add("s", this.startTime)
                .add("e", this.endTime)
                .add("tk", this.totalTicks)
                .add("tm", this.totalTime)
                .add("h", JsonUtil.mapArray(this.entries, (entry) -> entry.data.count == 0 ? null : entry.asJson()))
                .add("mp", JsonUtil.mapArray(this.minuteReports, MinuteReport::asJson))
                .build();
    }
}