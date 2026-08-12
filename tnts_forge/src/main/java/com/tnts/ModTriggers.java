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
 * - {@code tnts:double_jumped} -> impulso de explosion de las Botas de TNT
 * - {@code tnts:king_set} -> set bonus del Rey (criterio "pieces": minimo de piezas)
 * - {@code tnts:king_phase} -> fase visual del Rey (criterio "level": minimo de grietas)
 * - {@code tnts:king_guardian_defeated} -> derrotar al Rey guardian del almacen
 */
public class ModTriggers {

    public static final IgnitedTrigger IGNITED = new IgnitedTrigger();
    public static final ExplodedTrigger EXPLODED = new ExplodedTrigger();
    public static final MassDetonatedTrigger MASS_DETONATED = new MassDetonatedTrigger();
    public static final DoubleJumpTrigger DOUBLE_JUMPED = new DoubleJumpTrigger();
    public static final KingSetTrigger KING_SET = new KingSetTrigger();
    public static final KingPhaseTrigger KING_PHASE = new KingPhaseTrigger();
    public static final KingGuardianDefeatedTrigger KING_GUARDIAN_DEFEATED = new KingGuardianDefeatedTrigger();

    /** Registra los triggers (llamar en el constructor del mod). */
    public static void register() {
        CriteriaTriggers.register(IGNITED);
        CriteriaTriggers.register(EXPLODED);
        CriteriaTriggers.register(MASS_DETONATED);
        CriteriaTriggers.register(DOUBLE_JUMPED);
        CriteriaTriggers.register(KING_SET);
        CriteriaTriggers.register(KING_PHASE);
        CriteriaTriggers.register(KING_GUARDIAN_DEFEATED);
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

    // ---------- tnts:double_jumped ----------

    public static class DoubleJumpTrigger extends SimpleCriterionTrigger<SimpleInstance> {

        public static final ResourceLocation ID = new ResourceLocation(TntsMod.MODID, "double_jumped");

        @Override
        public ResourceLocation getId() {
            return ID;
        }

        public void trigger(ServerPlayer player) {
            this.trigger(player, inst -> true);
        }

        @Override
        protected SimpleInstance createInstance(JsonObject json, ContextAwarePredicate player,
                                                DeserializationContext context) {
            return new SimpleInstance(player);
        }
    }

    /** Instancia sin condiciones extra (solo el predicate de jugador). */
    public static class SimpleInstance extends AbstractCriterionTriggerInstance {

        public SimpleInstance(ContextAwarePredicate player) {
            super(new ResourceLocation(TntsMod.MODID, "simple"), player);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            return super.serializeToJson(context);
        }
    }

    // ---------- tnts:king_set ----------

    public static class KingSetTrigger extends SimpleCriterionTrigger<LevelInstance> {

        public static final ResourceLocation ID = new ResourceLocation(TntsMod.MODID, "king_set");

        @Override
        public ResourceLocation getId() {
            return ID;
        }

        /** Dispara con las piezas actuales del set (2, 3 o 4). */
        public void trigger(ServerPlayer player, int pieces) {
            this.trigger(player, inst -> inst.matches(pieces));
        }

        @Override
        protected LevelInstance createInstance(JsonObject json, ContextAwarePredicate player,
                                               DeserializationContext context) {
            return LevelInstance.fromJson(json, player);
        }
    }

    // ---------- tnts:king_phase ----------

    public static class KingPhaseTrigger extends SimpleCriterionTrigger<LevelInstance> {

        public static final ResourceLocation ID = new ResourceLocation(TntsMod.MODID, "king_phase");

        @Override
        public ResourceLocation getId() {
            return ID;
        }

        /** Dispara con el nivel de grietas alcanzado (1 = agrietado, 2 = muy agrietado). */
        public void trigger(ServerPlayer player, int level) {
            this.trigger(player, inst -> inst.matches(level));
        }

        @Override
        protected LevelInstance createInstance(JsonObject json, ContextAwarePredicate player,
                                               DeserializationContext context) {
            return LevelInstance.fromJson(json, player);
        }
    }

    /** Criterio comun: coincide si el valor actual >= el minimo pedido. */
    public static class LevelInstance extends AbstractCriterionTriggerInstance {

        private final int min;

        public LevelInstance(int min, ContextAwarePredicate player) {
            super(new ResourceLocation(TntsMod.MODID, "level"), player);
            this.min = min;
        }

        static LevelInstance fromJson(JsonObject json, ContextAwarePredicate player) {
            int min = json.has("min") ? GsonHelper.getAsInt(json, "min") : 1;
            return new LevelInstance(min, player);
        }

        public boolean matches(int actual) {
            return actual >= this.min;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            json.addProperty("min", this.min);
            return json;
        }
    }

    // ---------- tnts:king_guardian_defeated ----------

    public static class KingGuardianDefeatedTrigger extends SimpleCriterionTrigger<SimpleInstance> {

        public static final ResourceLocation ID = new ResourceLocation(TntsMod.MODID, "king_guardian_defeated");

        @Override
        public ResourceLocation getId() {
            return ID;
        }

        public void trigger(ServerPlayer player) {
            this.trigger(player, inst -> true);
        }

        @Override
        protected SimpleInstance createInstance(JsonObject json, ContextAwarePredicate player,
                                               DeserializationContext context) {
            return new SimpleInstance(player);
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
