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
import co.aikar.timings.TimingsEngine;
import com.google.common.collect.EvictingQueue;
import net.kyori.adventure.audience.Audience;
import org.apache.logging.log4j.Logger;
import org.inspirenxe.timings.core.history.MinuteReport;
import org.inspirenxe.timings.core.history.HistoryRecord;
import org.inspirenxe.timings.core.util.LoadingMap;
import org.inspirenxe.timings.core.util.MRUMapCache;
import org.spongepowered.api.Sponge;
import org.spongepowered.plugin.PluginContainer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

import javax.annotation.Nullable;

public final class VanillaTimingsEngine implements TimingsEngine {

    public final StandardTimingsEnvironment environment;
    public final Logger logger;
    public final EngineTickTiming engineTickTiming;
    public final AbstractTiming timingsTick;
    public final TicksTracker ticksTracker;
    public final Map<String, TimingIdentifier.TimingGroup> timingGroups;
    private final TimingIdentifier.TimingGroup defaultGroup;
    private final Map<TimingIdentifier, AbstractTiming> timingsByIdentifier;
    public final Collection<AbstractTiming> timings;
    public final ArrayDeque<MinuteReport> timingsReportsPerMinute;
    public final Queue<HistoryRecord> historicalReports;
    public final List<Audience> audiences;

    AbstractTiming currentTiming;
    boolean needsFullReset = false;
    boolean needsRecheckEnabled = false;

    public VanillaTimingsEngine(final Logger logger, final StandardTimingsEnvironment environment, final TimingIdentifier engineTickIdentifier) {
        this.environment = environment;
        this.logger = logger;

        // Order matters
        this.timingGroups = MRUMapCache.of(LoadingMap.newIdentityHashMap(TimingIdentifier.TimingGroup::new, 64));
        this.defaultGroup = this.group("Minecraft");
        this.timingsByIdentifier = Collections.synchronizedMap(LoadingMap.newHashMap(id -> new AbstractTiming.Instance(this, id),
                256, .5F));
        this.engineTickTiming = new EngineTickTiming(this, engineTickIdentifier);
        this.timingsTick = this.of("Timings Tick", this.engineTickTiming);
        this.ticksTracker = new TicksTracker();
        this.timings = new ArrayDeque<>();
        this.timingsReportsPerMinute = new ArrayDeque<>();
        this.historicalReports = EvictingQueue.create(12);
        this.audiences = new ArrayList<>();
    }

    @Override
    public AbstractTiming of(final PluginContainer plugin, final String name) {
        return this.of(Objects.requireNonNull(plugin, "plugin").metadata().id(), Objects.requireNonNull(name, "name"), null);
    }

    @Override
    public AbstractTiming of(final PluginContainer plugin, final String name, final Timing group) {
        return this.of(Objects.requireNonNull(plugin, "plugin").metadata().id(), Objects.requireNonNull(name, "name"), Objects.requireNonNull(group
                , "group"));
    }

    public AbstractTiming of(final String id, final String name, final @Nullable Timing group) {
        return this.timingsByIdentifier.get(new TimingIdentifier(id, name, group, true));
    }

    public AbstractTiming of(final String name, final Timing group) {
        return this.timingsByIdentifier.get(new TimingIdentifier(this.defaultGroup.name, name, group, true));
    }

    public AbstractTiming of(final String name) {
        return this.timingsByIdentifier.get(new TimingIdentifier(this.defaultGroup.name, name, null, true));
    }

    /**
     * Resets all timing data on the next tick
     */
    public void reset() {
        this.needsFullReset = true;
    }

    /**
     * Counts the number of times a timer caused TPS loss.
     */
    public void tick() {
        if (this.environment.enabled()) {
            boolean violated = this.engineTickTiming.violated();

            for (final AbstractTiming timing : this.timings) {
                if (this.isSpecialTiming(timing)) {
                    continue;
                }
                timing.processTick(violated);
            }

            this.ticksTracker.playerTicks += Sponge.server().onlinePlayers().size();
            this.ticksTracker.timedTicks++;
        }
    }

    public void stop() {
        this.environment.setEnabled(false);
        this.recheckEnabled();
    }

    void recheckEnabled() {
        synchronized (this.timingsByIdentifier) {
            for (final AbstractTiming timings : this.timingsByIdentifier.values()) {
                timings.checkEnabled();
            }
        }
        this.needsRecheckEnabled = false;
    }

    void resetTimings() {
        if (this.needsFullReset) {
            // Full resets need to re-check every handlers enabled state
            // Timing map can be modified from async so we must sync on it.
            synchronized (this.timingsByIdentifier) {
                for (final AbstractTiming timings : this.timingsByIdentifier.values()) {
                    timings.reset(true);
                }
            }
            if (this.ticksTracker.timingStart != 0) {
                this.logger.info("Timings reset");
            }
            this.historicalReports.clear();
            this.needsFullReset = false;
            this.needsRecheckEnabled = false;
            this.ticksTracker.timingStart = System.currentTimeMillis();
        } else {
            // Soft resets only need to act on timings that have done something
            // Handlers can only be modified on main thread.
            for (final AbstractTiming timings : this.timings) {
                timings.reset(false);
            }
        }

        this.timings.clear();
        this.timingsReportsPerMinute.clear();

        this.ticksTracker.resetTicks(true);
        this.ticksTracker.historyStart = System.currentTimeMillis();
    }

    @Nullable
    public TimingIdentifier.TimingGroup group(@Nullable String group) {
        if (group == null) {
            return this.defaultGroup;
        }

        return this.timingGroups.get(group.intern());
    }

    public boolean isSpecialTiming(final Timing timing) {
        return timing == this.engineTickTiming || timing == this.timingsTick;
    }
}