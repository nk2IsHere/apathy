package eu.nk2.apathy.context;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import blue.endless.jankson.annotation.SerializedName;
import blue.endless.jankson.api.SyntaxError;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class ApathyConfigLoader {

    private ApathyConfigLoader() {
    }

    public static ApathyConfig loadConfig() throws IOException {
        var jankson = Jankson
            .builder()
            .build();

        var configFile = FabricLoader
            .getInstance()
            .getConfigDir()
            .resolve("apathy.hjson")
            .toFile();

        if (!configFile.exists() && !configFile.createNewFile()) {
            return null;
        }

        try {
            JsonObject configObject = jankson.load(configFile);
            return ApathyConfig.fromJson(configObject);
        } catch (SyntaxError e) {
            Files.writeString(
                configFile.toPath(),
                ApathyConfig
                    .toDefaultJson()
                    .toJson(true, true, 0)
            );
            return ApathyConfig.getDefault();
        }
    }

    // TODO: write comments on save
    public static void saveConfig(ApathyConfig apathyConfig) throws IOException {
        var jankson = Jankson
            .builder()
            .build();

        var configFile = FabricLoader
            .getInstance()
            .getConfigDir()
            .resolve("apathy.hjson")
            .toFile();

        if (!configFile.exists() && !configFile.createNewFile()) {
            return;
        }

        Files.writeString(
            configFile.toPath(),
            jankson
                .toJson(apathyConfig.toJson())
                .toJson(true, true)
        );
    }
}
