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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.inspirenxe.timings.core.AbstractTiming;
import org.inspirenxe.timings.core.TimingData;
import org.inspirenxe.timings.core.util.JsonUtil;

class TimingHistoryEntry {

    final TimingData data;
    final TimingData[] children;
    TimingHistoryEntry(final AbstractTiming timing) {
        this.data = timing.data.copy();
        this.children = timing.copyChildren();
    }

    JsonElement asJson() {
        final JsonArray result = this.data.asJson();
        if (this.children.length > 0) {
            result.add(JsonUtil.mapArray(this.children, TimingData::asJson));
        }
        return result;
    }
}