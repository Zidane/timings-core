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
import org.inspirenxe.timings.core.TimingData;
import org.inspirenxe.timings.core.VanillaTimingsEngine;
import org.inspirenxe.timings.core.history.record.PingRecord;
import org.inspirenxe.timings.core.history.record.ServerTicksRecord;
import org.inspirenxe.timings.core.util.JsonUtil;
import org.spongepowered.api.Sponge;

import java.lang.management.ManagementFactory;

public final class MinuteReport {

    final long time;
    final ServerTicksRecord ticksRecord;
    final PingRecord pingRecord;
    final TimingData data;
    final double tps;
    final double usedMemory;
    final double freeMemory;
    final double loadAvg;

    public MinuteReport(final VanillaTimingsEngine manager) {
        this.time = System.currentTimeMillis() / 1000;
        this.ticksRecord = new ServerTicksRecord(manager.ticksTracker.timedTicks, manager.timingsReportsPerMinute.size());
        this.pingRecord = new PingRecord(Sponge.server().onlinePlayers());
        this.data = manager.engineTickTiming.minuteData.copy();
        this.tps = 1E9 / (System.nanoTime() - manager.ticksTracker.lastMinuteTime) * this.ticksRecord.timed;
        this.usedMemory = manager.engineTickTiming.avgUsedMemory;
        this.freeMemory = manager.engineTickTiming.avgFreeMemory;
        this.loadAvg = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();

    }

    public JsonElement asJson() {
        return JsonUtil.arrayOf(
                this.time,
                Math.round(this.tps * 100D) / 100D,
                Math.round(this.pingRecord.avg * 100D) / 100D,
                this.data.asJson(),
                this.ticksRecord.asJson(),
                this.usedMemory,
                this.freeMemory,
                this.loadAvg);
    }
}
