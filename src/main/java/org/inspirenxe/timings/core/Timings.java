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

import co.aikar.timings.TimingsService;
import com.google.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.inspirenxe.timings.core.command.TimingsCommand;
import org.inspirenxe.timings.core.duck.EngineDuck;
import org.spongepowered.api.Engine;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.util.HashSet;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Set;

@Plugin("timings")
public final class Timings {

    public static Timings instance() {
        return Timings.instance;
    }

    private static Timings instance;

    private final PluginContainer plugin;
    private final Logger logger;
    private final StandardTimingsEnvironment environment;
    private final Set<TimingsService> services;
    @Inject
    public Timings(final PluginContainer plugin, final Logger logger) {
        Timings.instance = this;
        this.plugin = plugin;
        this.logger = logger;
        this.environment = new StandardTimingsEnvironment();

        // Discover all our services
        this.services = new HashSet<>();
        final Iterator<TimingsService> iter = ServiceLoader.load(TimingsService.class).iterator();
        while (iter.hasNext()) {
            try {
                final TimingsService service = iter.next();
                this.services.add(service);
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Listener
    public void onConstructPlugin(final ConstructPluginEvent event) {
        this.initializeServices();
    }

    @Listener
    public void onServerStart(final StartingEngineEvent<Engine> event) {
        this.populateServiceTimings(event.engine());
    }

    @Listener
    public void onRegisterCommand(final RegisterCommandEvent<Command.Parameterized> event) {
        event.register(this.plugin, TimingsCommand.timingsCommand(), "timings");
    }

    public Logger logger() {
        return this.logger;
    }

    public StandardTimingsEnvironment environment() {
        return this.environment;
    }

    private void initializeServices() {
        for (final TimingsService service : ServiceLoader.load(TimingsService.class)) {
            service.initialize(this.environment);
        }
    }

    private void populateServiceTimings(final Engine engine) {
        for (final TimingsService service : ServiceLoader.load(TimingsService.class)) {
            service.populateTimings(this.environment, ((EngineDuck) engine).timingsManager());
        }
    }
}
