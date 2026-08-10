package com.tnts;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

/**
 * Triggers personalizados para advancements:
 * - {@code tnts:ignited}  -> se dispara al encender una TNT (mechero, redstone, detonador...)
 * - {@code tnts:exploded} -> se dispara cuando una TNT del mod explota
 * Ambos aceptan un criterio "block" opcional (id del bloque, ej. "tnts:mini_tnt");
 * si no se indica, coinciden con cualquier TNT.
 */
public class ModTriggers {

    public static final IgnitedTrigger IGNITED = new IgnitedTrigger();
    public static final ExplodedTrigger EXPLODED = new ExplodedTrigger();
    public static final MassDetonatedTrigger MASS_DETONATED = new MassDetonatedTrigger();

    /** Registra los triggers (llamar en el constructor del mod). */
    public static void register() {
        CriteriaTriggers.register(IGNITED);
        CriteriaTriggers.register(EXPLODED);
        CriteriaTriggers.register(MASS_DETONATED);
    }

    // ---------- tnts:ignited ----------

    public static class IgnitedTrigger extends SimpleCriterionTrigger<BlockInstance> {

        public static final ResourceLocation ID = new ResourceLocation(TntsMod.MODID, "ignited");

        @Override
        public ResourceLocation getId() {
            return ID;
        }

        public void trigger(ServerPlayer player, ResourceLocation block) {
            this.trigger(player, inst -> inst.matches(block));
        }

        @Override
        protected BlockInstance createInstance(JsonObject json, ContextAwarePredicate player,
                                               DeserializationContext context) {
            return BlockInstance.fromJson(json);
        }
    }

    // ---------- tnts:mass_detonated ----------

    public static class MassDetonatedTrigger extends SimpleCriterionTrigger<BlockInstance> {

        public static final ResourceLocation ID = new ResourceLocation(TntsMod.MODID, "mass_detonated");

        @Override
        public ResourceLocation getId() {
            return ID;
        }

        public void trigger(ServerPlayer player) {
            this.trigger(player, inst -> inst.matches(null));
        }

        @Override
        protected BlockInstance createInstance(JsonObject json, ContextAwarePredicate player,
                                               DeserializationContext context) {
            return BlockInstance.fromJson(json);
        }
    }

    // ---------- tnts:exploded ----------

    public static class ExplodedTrigger extends SimpleCriterionTrigger<BlockInstance> {

        public static final ResourceLocation ID = new ResourceLocation(TntsMod.MODID, "exploded");

        @Override
        public ResourceLocation getId() {
            return ID;
        }

        public void trigger(ServerPlayer player, ResourceLocation block) {
            this.trigger(player, inst -> inst.matches(block));
        }

        @Override
        protected BlockInstance createInstance(JsonObject json, ContextAwarePredicate player,
                                               DeserializationContext context) {
            return BlockInstance.fromJson(json);
        }
    }

    /** Criterio comun: coincide si el bloque coincide (o si no se pidio ninguno). */
    public static class BlockInstance extends AbstractCriterionTriggerInstance {

        private final ResourceLocation block;

        public BlockInstance(ResourceLocation block) {
            super(new ResourceLocation(TntsMod.MODID, "block"), ContextAwarePredicate.ANY);
            this.block = block;
        }

        static BlockInstance fromJson(JsonObject json) {
            ResourceLocation block = json.has("block")
                    ? new ResourceLocation(GsonHelper.getAsString(json, "block"))
                    : null;
            return new BlockInstance(block);
        }

        public boolean matches(ResourceLocation actual) {
            return block == null || block.equals(actual);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject json = new JsonObject();
            if (block != null) {
                json.addProperty("block", block.toString());
            }
            return json;
        }
    }
}
