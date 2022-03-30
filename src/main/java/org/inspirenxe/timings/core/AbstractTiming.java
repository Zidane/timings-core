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
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.inspirenxe.timings.core.util.LoadingIntMap;

public abstract class AbstractTiming implements Timing {

    private static int idPool = 1;
    private final VanillaTimingsEngine manager;
    public final int id = AbstractTiming.idPool++;
    public final String name;
    public final boolean verbose;
    public final Int2ObjectOpenHashMap<TimingData> children = new LoadingIntMap<>(TimingData::new);
    public final TimingData data;
    public final AbstractTiming group;
    private AbstractTiming parent;
    public long start = 0;
    public int timingDepth = 0;
    public boolean added;
    public boolean timed;
    public boolean enabled;

    protected AbstractTiming(final VanillaTimingsEngine manager, final TimingIdentifier id) {
        this.manager = manager;

        if (id.name.startsWith("##")) {
            this.verbose = true;
            this.name = id.name.substring(3);
        } else {
            this.name = id.name;
            this.verbose = false;
        }

        this.data = new TimingData(this.id);
        this.group = id.group;

        this.manager.group(id.groupId).timings.add(this);
        this.checkEnabled();
    }

    final void checkEnabled() {
        this.enabled = this.manager.environment.enabled() && (!this.verbose || this.manager.environment.verboseEnabled());
    }

    void processTick(final boolean violated) {
        if (this.timingDepth != 0 || this.data.currentTickCount == 0) {
            this.timingDepth = 0;
            this.start = 0;
            return;
        }

        this.data.processTick(violated);
        for (final TimingData data : this.children.values()) {
            data.processTick(violated);
        }
    }

    @Override
    public AbstractTiming start() {
        if (!this.enabled) {
            return this;
        }

        if (++this.timingDepth == 1) {
            this.start = System.nanoTime();
            this.parent = this.manager.currentTiming;
            this.manager.currentTiming = this;
        }
        return this;
    }

    @Override
    public void stop() {
        if (!this.enabled) {
            this.start = 0;
            return;
        }

        if (--this.timingDepth == 0 && this.start != 0) {
            this.addDiff(System.nanoTime() - this.start);
            this.start = 0;
        }
    }

    @Override
    public void abort() {
        if (this.enabled && this.timingDepth > 0) {
            this.start = 0;
        }
    }

    void addDiff(final long diff) {
        if (this.manager.currentTiming == this) {
            this.manager.currentTiming = this.parent;
            if (this.parent != null) {
                this.parent.children.get(this.id).add(diff);
            }
        }
        this.data.add(diff);
        if (!this.added) {
            this.added = true;
            this.timed = true;
            this.manager.timings.add(this);
        }
        if (this.group != null) {
            this.group.addDiff(diff);
            this.group.children.get(this.id).add(diff);
        }
    }

    public void reset(final boolean full) {
        this.data.reset();
        if (full) {
            this.timed = false;
        }
        this.start = 0;
        this.timingDepth = 0;
        this.added = false;
        this.children.clear();
        this.checkEnabled();
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof AbstractTiming)) {
            return false;
        }
        return (this == o);
    }

    @Override
    public int hashCode() {
        return this.id;
    }

    @Override
    public void close() {
        this.stop();
    }

    public TimingData[] copyChildren() {
        final TimingData[] copiedChildren = new TimingData[this.children.size()];
        int i = 0;
        for (final TimingData child : this.children.values()) {
            copiedChildren[i++] = child.copy();
        }
        return copiedChildren;
    }

    protected static class Instance extends AbstractTiming {

        protected Instance(final VanillaTimingsEngine manager, final TimingIdentifier id) {
            super(manager, id);
        }
    }
}