package eu.nk2.apathy.context;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ApathyConfig {
    public interface ApathyBehaviourType { }
    public static class ApathyBehaviourDoNotFollowType implements ApathyBehaviourType {
        @Override
        public String toString() {
            return "ApathyBehaviourDoNotFollowType{}";
        }
    }
    public static class ApathyBehaviourIfBlockBrokenType implements ApathyBehaviourType {
        private final float maximalReactionDistance;
        private final Block reactionBlock;

        public ApathyBehaviourIfBlockBrokenType(float maximalReactionDistance, Block reactionBlock) {
            this.maximalReactionDistance = maximalReactionDistance;
            this.reactionBlock = reactionBlock;
        }

        public float getMaximalReactionDistance() {
            return maximalReactionDistance;
        }

        public Block getReactionBlock() {
            return reactionBlock;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ApathyBehaviourIfBlockBrokenType that = (ApathyBehaviourIfBlockBrokenType) o;
            return Float.compare(that.maximalReactionDistance, maximalReactionDistance) == 0 && Objects.equals(reactionBlock, that.reactionBlock);
        }

        @Override
        public int hashCode() {
            return Objects.hash(maximalReactionDistance, reactionBlock);
        }

        @Override
        public String toString() {
            return "ApathyBehaviourIfBlockBrokenType{" +
                "maximalReactionDistance=" + maximalReactionDistance +
                ", reactionBlock=" + reactionBlock +
                '}';
        }
    }
    public static class ApathyBehaviourIfItemSelectedType implements ApathyBehaviourType {
        private final float maximalReactionDistance;
        private final Item reactionItem;
        private final int reactionItemCount;

        public ApathyBehaviourIfItemSelectedType(float maximalReactionDistance, Item reactionItem, int reactionItemCount) {
            this.maximalReactionDistance = maximalReactionDistance;
            this.reactionItem = reactionItem;
            this.reactionItemCount = reactionItemCount;
        }

        public float getMaximalReactionDistance() {
            return maximalReactionDistance;
        }

        public Item getReactionItem() {
            return reactionItem;
        }

        public int getReactionItemCount() {
            return reactionItemCount;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ApathyBehaviourIfItemSelectedType that = (ApathyBehaviourIfItemSelectedType) o;
            return Float.compare(that.maximalReactionDistance, maximalReactionDistance) == 0 && reactionItemCount == that.reactionItemCount && Objects.equals(reactionItem, that.reactionItem);
        }

        @Override
        public int hashCode() {
            return Objects.hash(maximalReactionDistance, reactionItem, reactionItemCount);
        }

        @Override
        public String toString() {
            return "ApathyBehaviourIfItemSelectedType{" +
                "maximalReactionDistance=" + maximalReactionDistance +
                ", reactionItem=" + reactionItem +
                ", reactionItemCount=" + reactionItemCount +
                '}';
        }
    }

    private final Map<Identifier, List<ApathyBehaviourType>> apathyBehaviourTypeMap;

    public ApathyConfig(Map<Identifier, List<ApathyBehaviourType>> apathyBehaviourTypeMap) {
        this.apathyBehaviourTypeMap = apathyBehaviourTypeMap;
    }

    public Map<Identifier, List<ApathyBehaviourType>> getApathyBehaviourTypeMap() {
        return apathyBehaviourTypeMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApathyConfig that = (ApathyConfig) o;
        return Objects.equals(apathyBehaviourTypeMap, that.apathyBehaviourTypeMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(apathyBehaviourTypeMap);
    }

    @Override
    public String toString() {
        return "ApathyConfig{" +
            "apathyBehaviourTypeMap=" + apathyBehaviourTypeMap +
            '}';
    }
}
