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
package org.inspirenxe.timings.core;

import org.inspirenxe.timings.core.export.TimingsExport;
import org.inspirenxe.timings.core.history.HistoryRecord;
import org.inspirenxe.timings.core.history.MinuteReport;

public final class EngineTickTiming extends AbstractTiming.Instance {
    private final VanillaTimingsEngine manager;
    public final TimingData minuteData;
    public double avgFreeMemory = -1D;
    public double avgUsedMemory = -1D;

    public EngineTickTiming(final VanillaTimingsEngine manager, final TimingIdentifier identifier) {
        super(manager, identifier);
        this.manager = manager;
        this.minuteData = new TimingData(this.id);
    }

    @Override
    public AbstractTiming start() {
        if (this.manager.needsFullReset) {
            this.manager.resetTimings();
        } else if (this.manager.needsRecheckEnabled) {
            this.manager.recheckEnabled();
        }
        super.start();
        return this;
    }

    @Override
    public void stop() {
        super.stop();
        if (!this.enabled) {
            return;
        }
        if (this.manager.ticksTracker.timedTicks % 20 == 0) {
            final Runtime runtime = Runtime.getRuntime();
            double usedMemory = runtime.totalMemory() - runtime.freeMemory();
            double freeMemory = runtime.maxMemory() - usedMemory;
            if (this.avgFreeMemory == -1) {
                this.avgFreeMemory = freeMemory;
            } else {
                this.avgFreeMemory = (this.avgFreeMemory * (59 / 60D)) + (freeMemory * (1 / 60D));
            }

            if (this.avgUsedMemory == -1) {
                this.avgUsedMemory = usedMemory;
            } else {
                this.avgUsedMemory = (this.avgUsedMemory * (59 / 60D)) + (usedMemory * (1 / 60D));
            }
        }

        long start = System.nanoTime();
        this.manager.tick();
        long diff = System.nanoTime() - start;
        this.manager.currentTiming = this.manager.timingsTick;
        this.manager.timingsTick.addDiff(diff);
        // addDiff for TIMINGS_TICK incremented this, bring it back down to 1
        // per tick.
        this.data.currentTickCount--;
        this.minuteData.currentTickTotal = this.data.currentTickTotal;
        this.minuteData.currentTickCount = 1;
        boolean violated = this.violated();
        this.minuteData.processTick(violated);
        this.manager.timingsTick.processTick(violated);
        this.processTick(violated);

        if (this.manager.ticksTracker.timedTicks % 1200 == 0) {
            this.manager.timingsReportsPerMinute.add(new MinuteReport(this.manager));
            this.manager.ticksTracker.resetTicks(false);
            this.minuteData.reset();
        }
        if (this.manager.ticksTracker.timedTicks % this.manager.environment.historyInterval() == 0) {
            this.manager.historicalReports.add(new HistoryRecord(this.manager));
            this.manager.resetTimings();
        }

        TimingsExport.report(this.manager);
    }

    boolean violated() {
        return this.data.currentTickTotal > 50000000;
    }
}