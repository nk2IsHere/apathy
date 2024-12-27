package eu.nk2.apathy.context;

import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public record ApathyConfig(Map<Identifier, List<ApathyBehaviourType>> apathyBehaviourTypeMap) {

    public enum ApathyBehaviourTypeFlag {
        DO_NOT_FOLLOW("do_not_follow"),
        IF_BLOCK_BROKEN("if_block_broken"),
        IF_ITEM_SELECTED("if_item_selected");

        private final String name;

        ApathyBehaviourTypeFlag(String name) {
            this.name = name;
        }

        public static ApathyBehaviourTypeFlag fromJson(JsonObject json) {
            return switch (json.get(
                String.class,
                "type"
            )) {
                case "do_not_follow" -> DO_NOT_FOLLOW;
                case "if_block_broken" -> IF_BLOCK_BROKEN;
                case "if_item_selected" -> IF_ITEM_SELECTED;
                case null -> throw new IllegalArgumentException("Missing ApathyBehaviourTypeFlag");
                default -> throw new IllegalArgumentException("Unknown ApathyBehaviourTypeFlag");
            };
        }

        public JsonPrimitive toJson() {
            return new JsonPrimitive(name);
        }
    }

    public sealed interface ApathyBehaviourType {
        JsonObject toJson();
    }

    public record ApathyBehaviourDoNotFollowType() implements ApathyBehaviourType {

        public static ApathyBehaviourDoNotFollowType fromJson(JsonObject json) {
            return new ApathyBehaviourDoNotFollowType();
        }

        public JsonObject toJson() {
            var jsonObject = new JsonObject();
            jsonObject.put(
                "type",
                ApathyBehaviourTypeFlag.DO_NOT_FOLLOW.toJson()
            );
            return jsonObject;
        }
    }

    public record ApathyBehaviourIfBlockBrokenType(
        float maximalReactionDistance,
        Block reactionBlock
    ) implements ApathyBehaviourType {

        public static ApathyBehaviourIfBlockBrokenType fromJson(JsonObject json) {
            var maximalReactionDistance = json.getFloat(
                "maximal_reaction_distance",
                10.0f
            );
            var reactionBlock = Optional
                .ofNullable(json.get(
                    String.class,
                    "reaction_block"
                ))
                .map(Identifier::tryParse)
                .map(Registries.BLOCK::get)
                .orElse(null);

            return new ApathyBehaviourIfBlockBrokenType(
                maximalReactionDistance,
                reactionBlock
            );
        }

        public JsonObject toJson() {
            var jsonObject = new JsonObject();
            jsonObject.put(
                "type",
                ApathyBehaviourTypeFlag.IF_BLOCK_BROKEN.toJson()
            );
            jsonObject.put(
                "maximal_reaction_distance",
                new JsonPrimitive(maximalReactionDistance())
            );
            jsonObject.put(
                "reaction_block",
                new JsonPrimitive(Registries.BLOCK
                    .getId(reactionBlock())
                    .toString())
            );
            return jsonObject;
        }
    }

    public record ApathyBehaviourIfItemSelectedType(
        float maximalReactionDistance,
        Item reactionItem,
        int reactionItemCount
    ) implements ApathyBehaviourType {

        public static ApathyBehaviourIfItemSelectedType fromJson(JsonObject json) {
            var maximalReactionDistance = json.getFloat(
                "maximal_reaction_distance",
                10.0f
            );
            var reactionItemCount = json.getInt(
                "reaction_item_count",
                1
            );
            var reactionItem = Optional
                .ofNullable(json.get(
                    String.class,
                    "reaction_item"
                ))
                .map(Identifier::tryParse)
                .map(Registries.ITEM::get)
                .orElse(null);

            return new ApathyBehaviourIfItemSelectedType(
                maximalReactionDistance,
                reactionItem,
                reactionItemCount
            );
        }

        public JsonObject toJson() {
            var jsonObject = new JsonObject();
            jsonObject.put(
                "type",
                ApathyBehaviourTypeFlag.IF_ITEM_SELECTED.toJson()
            );
            jsonObject.put(
                "maximal_reaction_distance",
                new JsonPrimitive(maximalReactionDistance())
            );
            jsonObject.put(
                "reaction_item",
                new JsonPrimitive(Registries.ITEM
                    .getId(reactionItem())
                    .toString())
            );
            jsonObject.put(
                "reaction_item_count",
                new JsonPrimitive(reactionItemCount())
            );
            return jsonObject;
        }
    }

    public static ApathyConfig fromJson(JsonObject json) {
        var apathyBehaviourTypeMap = json
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                entry -> entry
                    .getKey()
                    .equals("null")
                    ? null
                    : Identifier.tryParse(entry.getKey()),
                entry -> {
                    var value = (JsonArray) entry.getValue();
                    return value
                        .stream()
                        .map(JsonObject.class::cast)
                        .<ApathyBehaviourType>map(behaviourEntry -> switch (ApathyBehaviourTypeFlag.fromJson(behaviourEntry)) {
                            case DO_NOT_FOLLOW -> ApathyBehaviourDoNotFollowType.fromJson(behaviourEntry);
                            case IF_BLOCK_BROKEN -> ApathyBehaviourIfBlockBrokenType.fromJson(behaviourEntry);
                            case IF_ITEM_SELECTED -> ApathyBehaviourIfItemSelectedType.fromJson(behaviourEntry);
                        })
                        .toList();
                }
            ));

        return new ApathyConfig(apathyBehaviourTypeMap);
    }

    public static JsonObject toDefaultJson() {
        var defaultBehaviourEntryObject = new JsonObject();
        defaultBehaviourEntryObject.putDefault(
            "type",
            ApathyBehaviourTypeFlag.DO_NOT_FOLLOW.toJson(),
            """
                This is the type of a behaviour
                Allowed values are: do_not_follow, if_block_broken, if_item_selected
                For if_block_broken, if_item_selected you must include the maximal_reaction_distance property
                For if_block_broken you must include reaction_block property which is an Identifier of a block
                For if_item_selected you can include reaction_item which is an Identifier of an item and reaction_item_count to form ItemStack, else it is interpreted as any item amount
                """
        );
        var defaultBehaviourArray = new JsonArray();
        defaultBehaviourArray.add(defaultBehaviourEntryObject);

        var defaultBehaviourJsonObject = new JsonObject();
        defaultBehaviourJsonObject.putDefault(
            "null",
            defaultBehaviourArray,
            """
                This is the default behaviour for all mobs.
                You can configure multiple behaviours for any MobEntity
                The key is either null or an Identifier of a mob, the value is an array of behaviours described later
                """
        );

        return defaultBehaviourJsonObject;
    }

    public static ApathyConfig getDefault() {
        return fromJson(toDefaultJson());
    }

    public JsonObject toJson() {
        var jsonObject = new JsonObject();
        apathyBehaviourTypeMap.forEach((key, value) -> {
            var behaviours = new JsonArray();
            behaviours.addAll(
                value
                    .stream()
                    .map(ApathyBehaviourType::toJson)
                    .toList()
            );
            jsonObject.put(
                key == null ? "null" : key.toString(),
                behaviours
            );
        });
        return jsonObject;
    }
}
