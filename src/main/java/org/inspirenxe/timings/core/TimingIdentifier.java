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

import co.aikar.timings.Timing;

import java.util.ArrayDeque;

public final class TimingIdentifier {

    final String groupId;
    final String name;
    final AbstractTiming group;
    final boolean protect;
    private final int hashCode;

    public TimingIdentifier(final String groupId, final String name, final Timing group, final boolean protect) {
        this.groupId = groupId.intern();
        this.name = name.intern();
        this.group = group instanceof AbstractTiming ? (AbstractTiming) group : null;
        this.protect = protect;
        this.hashCode = (31 * this.groupId.hashCode()) + this.name.hashCode();
    }

    // We are using .intern() on the strings so it is guaranteed to be an
    // identity comparison.
    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof TimingIdentifier)) {
            return false;
        }
        final TimingIdentifier that = (TimingIdentifier) o;
        return this.groupId == that.groupId && this.name == that.name;
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    public static class TimingGroup {

        private static int idPool = 1;

        public final int id;
        public final String name;
        public final ArrayDeque<AbstractTiming> timings;
        TimingGroup(final String name) {
            this.id = TimingGroup.idPool++;
            this.name = name;
            this.timings = new ArrayDeque<>(64);
        }
    }
}