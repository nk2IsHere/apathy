# Apathy for 1.81.1, requires Fabric API

Make mobs apathetic up until you attack
Literally, thats it.

Or is it?\
Configuration example of `apathy.hjson` given:
```json5
{
  // This is the default behaviour for all mobs.
  // You can configure multiple behaviours for any MobEntity
  // The key is either null or an Identifier of a mob, the value is an array of behaviours described later
  "null": [
    {
      // This is the type of a behaviour
      // Allowed values are: do_not_follow, if_block_broken, if_item_selected
      // For if_block_broken, if_item_selected you must include the maximal_reaction_distance property
      // For if_block_broken you must include reaction_block property which is an Identifier of a block
      // For if_item_selected you must include reaction_item which is an Identifier of an item and reaction_item_count to form ItemStack
      "type": "do_not_follow"
    }
  ],
  
  // Zombie will be aggressive to a player, who has broken a sandstone block in radius 16.0 or shown any amount of enchanted gapples in any hand.
  "minecraft:zombie": [
    {
      "type": "if_block_broken",
      "maximal_reaction_distance": 16.0,
      "reaction_block": "minecraft:sandstone"
    },
    {
      "type": "if_item_selected",
      "maximal_reaction_distance": 16.0,
      "reaction_item": "minecraft:enchanted_golden_apple"
    }
  ],

  // Skeleton will be aggressive to a playes who has shown 16 zombie spawn eggs in any hand.
  "minecraft:skeleton": [
    {
      "type": "if_item_selected",
      "maximal_reaction_distance": 32.0,
      "reaction_item": "minecraft:zombie_spawn_egg",
      "reaction_item_count": 16
    }
  ]
}

```
