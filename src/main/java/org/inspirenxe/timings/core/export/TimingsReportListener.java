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
package org.inspirenxe.timings.core.export;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.Validate;
import org.spongepowered.api.SystemSubject;
import org.spongepowered.api.adventure.Audiences;

import java.util.List;
import java.util.Objects;

public final class TimingsReportListener {

    private final Runnable doneCallback;
    private String timingsURL;
    private final ForwardingAudience audience;

    public TimingsReportListener(final List<Audience> channels) {
        this(channels, null);
    }
    public TimingsReportListener(final List<Audience> channels, final Runnable doneCallback) {
        Objects.requireNonNull(channels);
        Validate.notEmpty(channels);

        this.addConsoleIfNeeded(channels);
        this.doneCallback = doneCallback;
        this.audience = Audience.audience(channels);
    }

    public String timingsUrl() {
        return this.timingsURL;
    }

    public void done() {
        this.done(null);
    }

    public void done(final String url) {
        this.timingsURL = url;
        if (this.doneCallback != null) {
            this.doneCallback.run();
        }
    }

    private void addConsoleIfNeeded(final List<Audience> channels) {
        boolean hasConsole = false;
        for (Audience channel: channels) {
            if (channel instanceof SystemSubject) {
                hasConsole = true;
                break;
            }
        }

        if (!hasConsole) {
            channels.add(Audiences.system());
        }
    }

    public void send(final Component text) {
        this.audience.sendMessage(Identity.nil(), text);
    }

    public ForwardingAudience channel() {
        return this.audience;
    }
}