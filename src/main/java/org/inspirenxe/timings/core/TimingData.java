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

import com.google.gson.JsonArray;
import org.inspirenxe.timings.core.util.JsonUtil;

import java.util.function.Function;

public final class TimingData {

    static Function<Integer, TimingData> LOADER = TimingData::new;
    public final int id;
    public int count = 0;
    public int lagCount = 0;
    public long totalTime = 0;
    public long lagTotalTime = 0;

    int currentTickCount = 0;
    long currentTickTotal = 0;

    TimingData(final int id) {
        this.id = id;
    }

    TimingData(final TimingData data) {
        this.id = data.id;
        this.totalTime = data.totalTime;
        this.lagTotalTime = data.lagTotalTime;
        this.count = data.count;
        this.lagCount = data.lagCount;
    }

    void add(final long diff) {
        ++this.currentTickCount;
        this.currentTickTotal += diff;
    }

    void processTick(final boolean violated) {
        this.totalTime += this.currentTickTotal;
        this.count += this.currentTickCount;
        if (violated) {
            this.lagTotalTime += this.currentTickTotal;
            this.lagCount += this.currentTickCount;
        }
        this.currentTickTotal = 0;
        this.currentTickCount = 0;
    }

    void reset() {
        this.count = 0;
        this.lagCount = 0;
        this.currentTickTotal = 0;
        this.currentTickCount = 0;
        this.totalTime = 0;
        this.lagTotalTime = 0;
    }

    public TimingData copy() {
        return new TimingData(this);
    }

    public JsonArray asJson() {
        return JsonUtil.arrayOf(
                this.id,
                this.count,
                this.totalTime,
                this.lagCount,
                this.lagTotalTime
        );
    }

    boolean hasData() {
        return this.count > 0;
    }
}