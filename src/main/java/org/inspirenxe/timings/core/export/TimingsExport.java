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

import static org.spongepowered.api.Platform.Component.IMPLEMENTATION;

import com.google.common.base.Joiner;
import com.google.gson.JsonObject;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minecraft.server.MinecraftServer;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.inspirenxe.timings.core.AbstractTiming;
import org.inspirenxe.timings.core.TimingIdentifier;
import org.inspirenxe.timings.core.VanillaTimingsEngine;
import org.inspirenxe.timings.core.history.HistoryRecord;
import org.inspirenxe.timings.core.util.JsonUtil;
import org.inspirenxe.timings.core.util.JsonUtil.JsonObjectBuilder;
import org.spongepowered.api.Platform;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.entity.BlockEntityType;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.network.RconConnection;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.metadata.model.PluginContributor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

public final class TimingsExport extends Thread {

    private static final Joiner RUNTIME_FLAG_JOINER = Joiner.on(" ");
    //private static final Joiner CONFIG_PATH_JOINER = Joiner.on(".");

    private final VanillaTimingsEngine manager;
    private final TimingsReportListener listeners;
    private final JsonObject out;
    private final HistoryRecord[] history;
    private static long lastReport = 0;
    public TimingsExport(final VanillaTimingsEngine manager, final TimingsReportListener listeners, final JsonObject out, final HistoryRecord[] history) {
        super("Timings paste thread");
        this.manager = manager;
        this.listeners = listeners;
        this.out = out;
        this.history = history;
    }

    private static String serverName() {
        final PluginContainer implementation = Sponge.platform().container(IMPLEMENTATION);
        return implementation.metadata().name().get() + " " + implementation.metadata().version().toString();
    }

    /**
     * Builds an XML report of the timings to be uploaded for parsing.
     */
    public static void report(final VanillaTimingsEngine manager) {
        if (manager.audiences.isEmpty()) {
            return;
        }
        final TimingsReportListener listeners = new TimingsReportListener(new ArrayList<>(manager.audiences));

        manager.audiences.clear();
        long now = System.currentTimeMillis();
        final long lastReportDiff = now - TimingsExport.lastReport;
        if (lastReportDiff < 60000) {
            listeners.send(Component.text("Please wait at least 1 minute in between Timings reports. (" + (int)((60000 - lastReportDiff) / 1000) + " seconds)", NamedTextColor.RED));
            listeners.done();
            return;
        }
        final long lastStartDiff = now - manager.ticksTracker.timingStart;
        if (lastStartDiff < 180000) {
            listeners.send(Component.text("Please wait at least 3 minutes before generating a Timings report. Unlike Timings v1, v2 benefits from longer timings and is not as useful with short timings. (" + (int)((180000 - lastStartDiff) / 1000) + " seconds)", NamedTextColor.RED));
            listeners.done();
            return;
        }
        listeners.send(Component.text("Preparing Timings Report...", NamedTextColor.GREEN));
        TimingsExport.lastReport = now;

        final Platform platform = Sponge.platform();
        final JsonObjectBuilder builder = JsonUtil.objectBuilder()
                // Get some basic system details about the server
                .add("version", platform.container(IMPLEMENTATION).metadata().version().toString())
                .add("maxplayers", Sponge.server().maxPlayers())
                .add("start", manager.ticksTracker.timingStart / 1000)
                .add("end", System.currentTimeMillis() / 1000)
                .add("sampletime", (System.currentTimeMillis() - manager.ticksTracker.timingStart) / 1000);
        if (!manager.environment.privacyMode()) {
            builder.add("server", TimingsExport.serverName())
                    .add("motd", PlainTextComponentSerializer.plainText().serialize(Sponge.server().motd()))
                    .add("online-mode", Sponge.server().isOnlineModeEnabled())
                    .add("icon", ((MinecraftServer) Sponge.server()).getStatus().getFavicon());
        }

        final Runtime runtime = Runtime.getRuntime();
        final RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        builder.add("system", JsonUtil.objectBuilder()
                .add("timingcost", TimingsExport.getCost(manager))
                .add("name", System.getProperty("os.name"))
                .add("version", System.getProperty("os.version"))
                .add("jvmversion", System.getProperty("java.version"))
                .add("arch", System.getProperty("os.arch"))
                .add("maxmem", runtime.maxMemory())
                .add("cpu", runtime.availableProcessors())
                .add("runtime", ManagementFactory.getRuntimeMXBean().getUptime())
                .add("flags", TimingsExport.RUNTIME_FLAG_JOINER.join(runtimeBean.getInputArguments()))
                .add("gc", JsonUtil.mapArrayToObject(ManagementFactory.getGarbageCollectorMXBeans(), (input) ->
                        JsonUtil.singleObjectPair(input.getName(), JsonUtil.arrayOf(input.getCollectionCount(), input.getCollectionTime())))));

        final Set<BlockEntityType> blockEntityTypes = new HashSet<>();
        final Set<EntityType<?>> entityTypes = new HashSet<>();

        final int size = manager.historicalReports.size();
        final HistoryRecord[] history = new HistoryRecord[size + 1];
        int i = 0;
        for (final HistoryRecord record : manager.historicalReports) {
            blockEntityTypes.addAll(record.blockEntityTypes);
            entityTypes.addAll(record.entityTypes);
            history[i++] = record;
        }

        history[i] = new HistoryRecord(manager); // Current snapshot
        blockEntityTypes.addAll(history[i].blockEntityTypes);
        entityTypes.addAll(history[i].entityTypes);

        final JsonObjectBuilder timingsBuilder = JsonUtil.objectBuilder();
        for (final TimingIdentifier.TimingGroup group : manager.timingGroups.values()) {
            for (final AbstractTiming timing : group.timings) {
                if (!timing.timed && !manager.isSpecialTiming(timing)) {
                    continue;
                }
                timingsBuilder.add(timing.id, JsonUtil.arrayOf(group.id, timing.name));
            }
        }

        builder
                .add("idmap", JsonUtil.objectBuilder()
                .add("groups", JsonUtil.mapArrayToObject(manager.timingGroups.values(), (group) ->
                        JsonUtil.singleObjectPair(group.id, group.name)))
                .add("handlers", timingsBuilder)
//                .add("worlds", JsonUtil.mapArrayToObject(HistoryRecord.worldMap.entrySet(), (entry) ->
//                        JsonUtil.singleObjectPair(entry.getValue(), entry.getKey())))
                .add("blockentity", JsonUtil.mapArrayToObject(blockEntityTypes, (blockEntityType) ->
                        {
                            final ResourceKey resourceKey = Sponge.game().registry(RegistryTypes.BLOCK_ENTITY_TYPE).valueKey(blockEntityType);
                            return JsonUtil.singleObjectPair(TimingIdHelper.idFor(blockEntityType), resourceKey);
                        })
                )
                .add("entity", JsonUtil.mapArrayToObject(entityTypes, (entityType) ->
                        {
                            final ResourceKey resourceKey = Sponge.game().registry(RegistryTypes.ENTITY_TYPE).valueKey(entityType);
                            return JsonUtil.singleObjectPair(TimingIdHelper.idFor(entityType), resourceKey);
                        })
                )
        );

        // Information about loaded plugins

        builder.add("plugins", JsonUtil.mapArrayToObject(Sponge.pluginManager().plugins(), (plugin) -> {
            final @Nullable URL homepageUrl = plugin.metadata().links().homepage().orElse(null);
            final String homepage = homepageUrl != null ? homepageUrl.toString() : "<not specified>";
            return JsonUtil.objectBuilder().add(plugin.metadata().id(), JsonUtil.objectBuilder()
                    .add("version", plugin.metadata().version().toString())
                    .add("description", plugin.metadata().description().orElse(""))
                    .add("website", homepage)
                    .add("authors", plugin.metadata().contributors().stream().map(PluginContributor::name).collect(Collectors.joining(", ")))
            ).build();
        }));

        // Information on the users Config

//        builder
//                .add("config", JsonUtil.objectBuilder()
//                    .add("sponge", TimingsExport.serializeConfigNode(SpongeConfigs.getCommon().getNode()))
//                );

        new TimingsExport(manager, listeners, builder.build(), history).start();
    }

    static long getCost(final VanillaTimingsEngine manager) {
        // Benchmark the users System.nanotime() for cost basis
        int passes = 200;
        final AbstractTiming SAMPLER1 = manager.of("Timings Sampler 1");
        final AbstractTiming SAMPLER2 = manager.of("Timings Sampler 2");
        final AbstractTiming SAMPLER3 = manager.of("Timings Sampler 3");
        final AbstractTiming SAMPLER4 = manager.of("Timings Sampler 4");
        final AbstractTiming SAMPLER5 = manager.of("Timings Sampler 5");
        final AbstractTiming SAMPLER6 = manager.of("Timings Sampler 6");

        final long start = System.nanoTime();
        for (int i = 0; i < passes; i++) {
            SAMPLER1.start();
            SAMPLER2.start();
            SAMPLER3.start();
            SAMPLER3.start();
            SAMPLER4.start();
            SAMPLER5.start();
            SAMPLER6.start();
            SAMPLER6.start();
            SAMPLER5.start();
            SAMPLER4.start();
            SAMPLER2.start();
            SAMPLER1.start();
        }
        final long timingsCost = (System.nanoTime() - start) / passes / 6;
        SAMPLER1.reset(true);
        SAMPLER2.reset(true);
        SAMPLER3.reset(true);
        SAMPLER4.reset(true);
        SAMPLER5.reset(true);
        SAMPLER6.reset(true);
        return timingsCost;
    }

//    private static JsonElement serializeConfigNode(final ConfigurationNode node) {
//        if (node.isMap()) {
//            final JsonObject object = new JsonObject();
//            for (final Entry<Object, ? extends ConfigurationNode> entry : node.childrenMap().entrySet()) {
//                final String fullPath = TimingsExport.CONFIG_PATH_JOINER.join(entry.getValue().path());
//                if (fullPath.equals("sponge.sql") || TimingsManager.hiddenConfigs.contains(fullPath)) {
//                    continue;
//                }
//                object.add(entry.getKey().toString(), TimingsExport.serializeConfigNode(entry.getValue()));
//            }
//            return object;
//        }
//        if (node.isList()) {
//            final JsonArray array = new JsonArray();
//            for (final ConfigurationNode child : node.childrenList()) {
//                array.add(TimingsExport.serializeConfigNode(child));
//            }
//            return array;
//        }
//        return JsonUtil.toJsonElement(node.raw());
//    }

    @Override
    public synchronized void start() {
        boolean containsRconSource = false;
        for (final Audience receiver : this.listeners.channel().audiences()) {
            if (receiver instanceof RconConnection) {
                containsRconSource = true;
                break;
            }
        }
        if (containsRconSource) {
            this.listeners.send(Component.text("Warning: Timings report done over RCON will cause lag spikes.", NamedTextColor.RED));
            this.listeners.send(Component.text("You should use ", NamedTextColor.RED).append(Component.text("/sponge timings report",
                    NamedTextColor.YELLOW)).append(Component.text(" in game or console.", NamedTextColor.RED)));
            this.run();
        } else {
            super.start();
        }
    }

    @Override
    public void run() {
        this.out.add("data", JsonUtil.mapArray(this.history, HistoryRecord::asJson));

        String response = null;
        String timingsURL = null;
        try {
            String hostname = "localhost";
            if (!this.manager.environment.privacyMode()) {
                try {
                    hostname = InetAddress.getLocalHost().getHostName();
                } catch (final IOException e) {
                    this.manager.logger.warn("Could not get own server hostname when uploading timings - falling back to 'localhost'", e);
                }
            }
            final HttpURLConnection con = (HttpURLConnection) new URL("https://timings.aikar.co/post").openConnection();
            con.setDoOutput(true);
            final String name = this.manager.environment.privacyMode() ? "" : TimingsExport.serverName();
            con.setRequestProperty("User-Agent", "Sponge/" + name + "/" + hostname);
            con.setRequestMethod("POST");
            con.setInstanceFollowRedirects(false);

            final OutputStream request = new GZIPOutputStream(con.getOutputStream()) {
                {
                    this.def.setLevel(7);
                }
            };

            this.manager.logger.error(JsonUtil.toString(this.out));
            request.write(JsonUtil.toString(this.out).getBytes(StandardCharsets.UTF_8));
            request.close();

            response = this.response(con);

            if (con.getResponseCode() != 302) {
                this.listeners.send(Component.text("Upload Error: " + con.getResponseCode() + ": " + con.getResponseMessage(), NamedTextColor.RED));
                this.listeners.send(Component.text("Check your logs for more information", NamedTextColor.RED));
                if (response != null) {
                    this.manager.logger.fatal(response);
                }
                return;
            }

            timingsURL = con.getHeaderField("Location");
            this.listeners.send(Component.text().content("View Timings Report: ").color(NamedTextColor.GREEN).append(Component.text(timingsURL).clickEvent(ClickEvent.openUrl(timingsURL))).build());

            if (response != null && !response.isEmpty()) {
                this.manager.logger.info("Timing Response: " + response);
            }
        } catch (final IOException ex) {
            this.listeners.send(Component.text("Error uploading timings, check your logs for more information", NamedTextColor.RED));
            if (response != null) {
                this.manager.logger.fatal(response);
            }
            this.manager.logger.fatal("Could not paste timings", ex);
        } finally {
            this.listeners.done(timingsURL);
        }
    }

    private String response(final HttpURLConnection con) throws IOException {
        try (final InputStream is = con.getInputStream()) {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();

            final byte[] b = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(b)) != -1) {
                bos.write(b, 0, bytesRead);
            }
            try {
                return bos.toString();
            } finally {
                bos.close();
            }
        } catch (final IOException ex) {
            this.listeners.send(Component.text("Error uploading timings, check your logs for more information", NamedTextColor.RED));
            this.manager.logger.warn(con.getResponseMessage(), ex);
            return null;
        }
    }
}