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
package org.inspirenxe.timings.core.command;

import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.inspirenxe.timings.core.VanillaTimingsEngine;
import org.inspirenxe.timings.core.duck.EngineDuck;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;

public final class TimingsCommand {

    public static Command.Parameterized timingsCommand() {
        return Command.builder()
                .permission("sponge.command.timings")
                .shortDescription(Component.text("Manages Sponge Timings data to see performance of the server."))
                .addChild(Command.builder()
                        .executor(context -> {
                            final VanillaTimingsEngine manager = ((EngineDuck) Sponge.server()).timingsManager();
                            if (!manager.environment.enabled()) {
                                return CommandResult.error(Component.text("Please enable timings by typing /sponge timings on"));
                            }
                            ((EngineDuck) Sponge.server()).timingsManager().reset();
                            context.sendMessage(Identity.nil(), Component.text("Timings reset"));
                            return CommandResult.success();
                        })
                        .build(), "reset")
                .addChild(Command.builder()
                        .executor(context -> {
                            final VanillaTimingsEngine manager = ((EngineDuck) Sponge.server()).timingsManager();
                            if (!manager.environment.enabled()) {
                                return CommandResult.error(Component.text("Please enable timings by typing /sponge timings on"));
                            }
                            manager.audiences.add(context.cause().audience());
                            return CommandResult.success();
                        })
                        .build(), "report", "paste")
                .addChild(Command.builder()
                        .executor(context -> {
                            final VanillaTimingsEngine manager = ((EngineDuck) Sponge.server()).timingsManager();
                            manager.environment.setEnabled(true);
                            manager.reset();
                            context.sendMessage(Identity.nil(), Component.text("Enabled Timings & Reset"));
                            return CommandResult.success();
                        })
                        .build(), "on")
                .addChild(Command.builder()
                        .executor(context -> {
                            final VanillaTimingsEngine manager = ((EngineDuck) Sponge.server()).timingsManager();
                            manager.environment.setEnabled(false);
                            context.sendMessage(Identity.nil(), Component.text("Disabled Timings"));
                            return CommandResult.success();
                        })
                        .build(), "off")
                .addChild(Command.builder()
                        .executor(context -> {
                            final VanillaTimingsEngine manager = ((EngineDuck) Sponge.server()).timingsManager();

                            if (!manager.environment.enabled()) {
                                return CommandResult.error(Component.text("Please enable timings by typing /sponge timings on"));
                            }
                            manager.environment.setVerboseEnabled(true);
                            context.sendMessage(Identity.nil(), Component.text("Enabled Verbose Timings"));
                            return CommandResult.success();
                        })
                        .build(), "verbon")
                .addChild(Command.builder()
                        .executor(context -> {
                            final VanillaTimingsEngine manager = ((EngineDuck) Sponge.server()).timingsManager();

                            if (!manager.environment.enabled()) {
                                return CommandResult.error(Component.text("Please enable timings by typing /sponge timings on"));
                            }
                            manager.environment.setVerboseEnabled(false);
                            context.sendMessage(Identity.nil(), Component.text("Disabled Verbose Timings"));
                            return CommandResult.success();
                        })
                        .build(), "verboff")
                .build();
    }

    private TimingsCommand() {
    }
}
