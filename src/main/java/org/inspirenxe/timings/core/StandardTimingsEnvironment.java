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

import co.aikar.timings.TimingsEnvironment;

public final class StandardTimingsEnvironment implements TimingsEnvironment {

    private boolean enabled = true;
    private boolean verboseEnabled = true;
    private boolean privacyMode = false;

    private int historyInterval = 60;
    private int historyLength = 60;

    @Override
    public boolean enabled() {
        return this.enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean verboseEnabled() {
        return this.verboseEnabled;
    }

    public void setVerboseEnabled(final boolean verboseEnabled) {
        this.verboseEnabled = verboseEnabled;
    }

    public boolean privacyMode() {
        return this.privacyMode;
    }

    @Override
    public int historyInterval() {
        return this.historyInterval;
    }

    @Override
    public int historyLength() {
        return this.historyLength;
    }
}
