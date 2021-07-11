package eu.nk2.apathy.context;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import blue.endless.jankson.api.SyntaxError;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class ApathyConfigLoader {

    enum ApathyBehaviourType {
        do_not_follow,
        if_block_broken,
        if_item_selected
    }

    public static ApathyConfig loadConfig() throws IOException {
        Jankson jankson = Jankson.builder()
            .build();
        File configFile = FabricLoader.getInstance().getConfigDir()
            .resolve("apathy.hjson")
            .toFile();
        if(!configFile.exists() && !configFile.createNewFile())
            return null;

        try {
            JsonObject configObject = jankson.load(configFile);
            Map<Identifier, List<ApathyConfig.ApathyBehaviourType>> apathyBehaviourTypeConfig =
                configObject.entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                        entry -> entry.getKey().equals("null") ? null : Identifier.tryParse(entry.getKey()),
                        entry -> {
                            JsonArray value = (JsonArray) entry.getValue();
                            return value.stream()
                                .map(behaviourEntry -> {
                                    JsonObject behaviourEntryObject = (JsonObject) behaviourEntry;
                                    switch (ApathyBehaviourType.valueOf(Objects.requireNonNull(behaviourEntryObject.get(String.class, "type")))) {
                                        case do_not_follow:
                                            return new ApathyConfig.ApathyBehaviourDoNotFollowType();
                                        case if_block_broken:
                                            return new ApathyConfig.ApathyBehaviourIfBlockBrokenType(
                                                behaviourEntryObject.get(Float.class, "maximal_reaction_distance"),
                                                Registry.BLOCK.get(Identifier.tryParse(behaviourEntryObject.get(String.class, "reaction_block")))
                                            );
                                        case if_item_selected:
                                            Integer reactionItemCount = behaviourEntryObject.get(Integer.class, "reaction_item_count");
                                            return new ApathyConfig.ApathyBehaviourIfItemSelectedType(
                                                behaviourEntryObject.get(Float.class, "maximal_reaction_distance"),
                                                Registry.ITEM.get(Identifier.tryParse(behaviourEntryObject.get(String.class, "reaction_item"))),
                                                reactionItemCount != null? reactionItemCount : 0
                                            );
                                    }

                                    return null;
                                })
                                .collect(Collectors.toList());
                        }
                    ));
            return new ApathyConfig(apathyBehaviourTypeConfig);
        } catch (SyntaxError e) {
            JsonObject defaultBehaviourEntryObject = new JsonObject();
            defaultBehaviourEntryObject.putDefault("type", new JsonPrimitive(ApathyBehaviourType.do_not_follow.name()),
                "This is the type of a behaviour\n" +
                "Allowed values are: do_not_follow, if_block_broken, if_item_selected\n" +
                "For if_block_broken, if_item_selected you must include the maximal_reaction_distance property\n" +
                "For if_block_broken you must include reaction_block property which is an Identifier of a block\n" +
                "For if_item_selected you can include reaction_item which is an Identifier of an item and reaction_item_count to form ItemStack, else it is interpreted as any item amount"
            );
            JsonArray defaultBehaviourArray = new JsonArray();
            defaultBehaviourArray.add(defaultBehaviourEntryObject);
            JsonObject defaultBehaviourJsonObject = new JsonObject();
            defaultBehaviourJsonObject.putDefault("null", defaultBehaviourArray,
                "This is the default behaviour for all mobs.\n" +
                "You can configure multiple behaviours for any MobEntity\n" +
                "The key is either null or an Identifier of a mob, the value is an array of behaviours described later"
            );
            Files.write(
                configFile.toPath(),
                defaultBehaviourJsonObject.toJson(true, true, 0)
                    .getBytes(StandardCharsets.UTF_8)
            );
            return new ApathyConfig(new HashMap<Identifier, List<ApathyConfig.ApathyBehaviourType>>() {{
                this.put(null, new ArrayList<ApathyConfig.ApathyBehaviourType>() {{
                    this.add(new ApathyConfig.ApathyBehaviourDoNotFollowType());
                }});
            }});
        }
    }

    // TODO: write comments on save
    public static void saveConfig(ApathyConfig apathyConfig) throws IOException {
        Jankson jankson = Jankson.builder()
            .build();
        File configFile = FabricLoader.getInstance().getConfigDir()
            .resolve("apathy.hjson")
            .toFile();

        if(!configFile.exists() && !configFile.createNewFile())
            return;

        JsonObject configObject = new JsonObject();
        apathyConfig.getApathyBehaviourTypeMap()
            .forEach((identifier, apathyBehaviourTypes) -> {
                JsonArray behaviours = new JsonArray();
                behaviours.addAll(
                    apathyBehaviourTypes.stream()
                        .map(apathyBehaviourType -> {
                            JsonObject value = new JsonObject();
                            if(apathyBehaviourType instanceof ApathyConfig.ApathyBehaviourDoNotFollowType) {
                                value.put("type", new JsonPrimitive(ApathyBehaviourType.do_not_follow.name()));
                            } else if(apathyBehaviourType instanceof ApathyConfig.ApathyBehaviourIfBlockBrokenType) {
                                ApathyConfig.ApathyBehaviourIfBlockBrokenType ifBlockBrokenBehaviour = (ApathyConfig.ApathyBehaviourIfBlockBrokenType) apathyBehaviourType;
                                value.put("type", new JsonPrimitive(ApathyBehaviourType.if_block_broken.name()));
                                value.put("maximal_reaction_distance", new JsonPrimitive(ifBlockBrokenBehaviour.getMaximalReactionDistance()));
                                value.put("reaction_block", new JsonPrimitive(
                                    Registry.BLOCK.getId(ifBlockBrokenBehaviour.getReactionBlock()).toString()
                                ));
                            } else if(apathyBehaviourType instanceof ApathyConfig.ApathyBehaviourIfItemSelectedType) {
                                ApathyConfig.ApathyBehaviourIfItemSelectedType ifItemSelectedBehaviour = (ApathyConfig.ApathyBehaviourIfItemSelectedType) apathyBehaviourType;
                                value.put("type", new JsonPrimitive(ApathyBehaviourType.if_item_selected.name()));
                                value.put("maximal_reaction_distance", new JsonPrimitive(ifItemSelectedBehaviour.getMaximalReactionDistance()));
                                value.put("reaction_item", new JsonPrimitive(
                                    Registry.ITEM.getId(ifItemSelectedBehaviour.getReactionItem()).toString()
                                ));
                                value.put("reaction_item_count", new JsonPrimitive(ifItemSelectedBehaviour.getReactionItemCount()));
                            }

                            return value;
                        })
                        .collect(Collectors.toList())
                );

                configObject.put(identifier.toString(), behaviours);
            });

        Files.write(
            configFile.toPath(),
            jankson.toJson(configObject)
                .toJson(true, true)
                .getBytes(StandardCharsets.UTF_8)
        );
    }
}
