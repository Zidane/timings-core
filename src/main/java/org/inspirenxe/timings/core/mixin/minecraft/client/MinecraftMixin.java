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
package org.inspirenxe.timings.core.mixin.minecraft.client;

import net.minecraft.client.Minecraft;
import org.inspirenxe.timings.core.TimingIdentifier;
import org.inspirenxe.timings.core.Timings;
import org.inspirenxe.timings.core.VanillaTimingsEngine;
import org.inspirenxe.timings.core.duck.EngineDuck;
import org.spongepowered.api.Engine;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin implements Engine, EngineDuck {

    private VanillaTimingsEngine timingsManager;
    @Override
    public VanillaTimingsEngine timingsManager() {
        if (this.timingsManager == null) {
            this.timingsManager = new VanillaTimingsEngine(Timings.instance().logger(), Timings.instance().environment(),
                    new TimingIdentifier("Minecraft", "Client Tick", null, false));
        }

        return this.timingsManager;
    }
}
