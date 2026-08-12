package com.tnts.entity;

import com.tnts.ModBlocks;
import com.tnts.ModItems;
import com.tnts.ModSounds;
import com.tnts.block.TntBlock;
import com.tnts.config.TntsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * GOLEM DE TNT: un mob aliado construido con bloques de TNT que sigue a su
 * dueño como un golem de hierro en miniatura.
 * <p>
 * Mecanicas:
 * <ul>
 *   <li>Se invoca con su huevo (o con una TNT + bloque de hierro en el altar).</li>
 *   <li>Lleva EQUIPADA una variante de TNT: al darle click derecho con una TNT
 *       del mod se la cambias.</li>
 *   <li>Al darle click derecho con MECHERO explota con el efecto COMPLETO de
 *       la variante que lleva (reutiliza TntsPrimedTnt.explode).</li>
 *   <li>Al morir (cualquier causa) explota tambien, como un golem de TNT
 *       deberia.</li>
 * </ul>
 */
public class TntGolemEntity extends PathfinderMob {

    private static final EntityDataAccessor<String> DATA_VARIANT =
            SynchedEntityData.defineId(TntGolemEntity.class, EntityDataSerializers.STRING);

    /** Jugador que lo invoco (lo sigue). */
    private UUID ownerUuid;

    /** Ultimo jugador que lo exploto (para el advancement). */
    private UUID lastKillerUuid;

    public TntGolemEntity(EntityType<? extends TntGolemEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.ARMOR_TOUGHNESS, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6)
                .add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    public static void onAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(TntsEntities.TNT_GOLEM.get(), createAttributes().build());
    }

    /** Pone al golem bajo el mando de un jugador. */
    public void setOwner(UUID owner) {
        this.ownerUuid = owner;
    }

    public UUID getOwnerUuid() {
        return this.ownerUuid;
    }

    /** Variante de TNT equipada (ej: "mega_tnt"). */
    public String getVariant() {
        return this.entityData.get(DATA_VARIANT);
    }

    public void setVariant(String variant) {
        this.entityData.set(DATA_VARIANT, variant);
    }

    /** La TNT equipada como bloque (para el renderer). */
    public net.minecraft.world.level.block.state.BlockState getVariantBlock() {
        var block = ForgeRegistries.BLOCKS.getValue(
                new net.minecraft.resources.ResourceLocation("tnts:" + getVariant()));
        if (block instanceof TntBlock) return block.defaultBlockState();
        return ModBlocks.MINI_TNT.get().defaultBlockState();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_VARIANT, "mini_tnt");
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new GolemFollowOwnerGoal(this, 1.15, 4.0F, 18.0F, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 10.0F));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        // humo de la mecha siempre encendida (es un golem de TNT)
        ServerLevel sl = (ServerLevel) this.level();
        if (this.tickCount % 8 == 0) {
            sl.sendParticles(ParticleTypes.SMOKE,
                    this.getX(), this.getY() + 1.05, this.getZ(),
                    com.tnts.config.TntsConfig.particles(1), 0.1, 0.05, 0.1, 0.0);
        }
        if (this.tickCount % 20 == 0) {
            sl.sendParticles(ParticleTypes.SMALL_FLAME,
                    this.getX(), this.getY() + 1.15, this.getZ(),
                    com.tnts.config.TntsConfig.particles(1), 0.05, 0.02, 0.05, 0.01);
        }
    }

    // ---------- interaccion: equipar TNT / explotar con mechero ----------

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.level().isClientSide) return InteractionResult.sidedSuccess(true);
        ItemStack stack = player.getItemInHand(hand);

        // mechero o carga de fuego -> EXPLOTA con la variante equipada
        if (stack.is(Items.FLINT_AND_STEEL) || stack.is(Items.FIRE_CHARGE)) {
            explodeNow();
            if (stack.is(Items.FLINT_AND_STEEL)) {
                stack.hurtAndBreak(1, player, (living) -> living.broadcastBreakEvent(hand));
            } else {
                stack.shrink(1);
            }
            return InteractionResult.sidedSuccess(true);
        }

        // una TNT del mod en la mano -> se la equipas (cambia la variante)
        Item item = stack.getItem();
        String variant = null;
        if (item instanceof net.minecraft.world.item.BlockItem bi
                && bi.getBlock() instanceof TntBlock) {
            var rl = ForgeRegistries.ITEMS.getKey(item);
            if (rl != null && rl.getNamespace().equals("tnts")) {
                variant = rl.getPath();
            }
        }
        if (variant != null) {
            this.setVariant(variant);
            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.END_ROD,
                        this.getX(), this.getY() + 1, this.getZ(),
                        12, 0.3, 0.3, 0.3, 0.03);
                sl.playSound(null, this.blockPosition(), net.minecraft.sounds.SoundEvents.ITEM_PICKUP,
                        SoundSource.NEUTRAL, 0.8F, 1.2F);
            }
            return InteractionResult.sidedSuccess(true);
        }

        return InteractionResult.PASS;
    }

    /**
     * La EXPLOSION del golem: reutiliza exactamente la misma logica que la TNT
     * encendida (efectos completos de la variante), para que un golem con una
     * TNT masiva haga el mismo desastre que la TNT.
     */
    public void explodeNow() {
        if (this.level().isClientSide) return;
        // crea una TNT encendida fantasma en su posicion y la hace explotar ya
        // (misma explosion que la TNT real, con el mismo dueño)
        TntsPrimedTnt ghost = new TntsPrimedTnt(this.level(),
                this.getX(), this.getY(), this.getZ(), "tnts:" + getVariant(), 1, this);
        ghost.setStationary(true);
        this.level().addFreshEntity(ghost);
        // la entidad fantasma explota en el siguiente tick (mecha 1)
        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.blockPosition(), ModSounds.BEEP.get(),
                    SoundSource.NEUTRAL, 1.0F, 0.5F);
        }
        this.discard();
    }

    // ---------- muerte: siempre explota ----------

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (source.getEntity() instanceof Player killer) {
            this.lastKillerUuid = killer.getUUID();
        }
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        // si muere (no si la desactivan), explota
        if (reason == net.minecraft.world.entity.Entity.RemovalReason.KILLED
                && !this.level().isClientSide) {
            explodeNow();
        }
        super.remove(reason);
    }

    // ---------- guardado ----------

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("GolemOwner")) this.ownerUuid = tag.getUUID("GolemOwner");
        if (tag.contains("GolemVariant")) this.setVariant(tag.getString("GolemVariant"));
        if (tag.contains("GolemKiller")) this.lastKillerUuid = tag.getUUID("GolemKiller");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.ownerUuid != null) tag.putUUID("GolemOwner", this.ownerUuid);
        tag.putString("GolemVariant", this.getVariant());
        if (this.lastKillerUuid != null) tag.putUUID("GolemKiller", this.lastKillerUuid);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return net.minecraft.sounds.SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return net.minecraft.sounds.SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    /**
     * Sigue al jugador que lo invoco (como un golem de hierro aliado).
     * Goal propio porque FollowOwnerGoal exige un TamableAnimal y este
     * golem no lo es — aqui solo caminamos hacia el dueño si esta lejos.
     */
    static class GolemFollowOwnerGoal extends net.minecraft.world.entity.ai.goal.Goal {
        private final TntGolemEntity golem;
        private final double speed;
        private final float startDist;
        private final float stopDist;
        private int recalc = 0;

        GolemFollowOwnerGoal(TntGolemEntity golem, double speed, float startDist, float stopDist, boolean canFly) {
            this.golem = golem;
            this.speed = speed;
            this.startDist = startDist;
            this.stopDist = stopDist;
        }

        @Override
        public boolean canUse() {
            if (this.golem.getOwnerUuid() == null) return false;
            Player owner = this.golem.level().getPlayerByUUID(this.golem.getOwnerUuid());
            if (owner == null) return false;
            if (owner.isSpectator()) return false;
            return this.golem.distanceToSqr(owner) >= this.startDist * this.startDist;
        }

        @Override
        public boolean canContinueToUse() {
            Player owner = this.golem.level().getPlayerByUUID(this.golem.getOwnerUuid());
            if (owner == null) return false;
            if (this.golem.getNavigation().isDone()) return false;
            return this.golem.distanceToSqr(owner) > this.stopDist * this.stopDist;
        }

        @Override
        public void start() {
            this.recalc = 0;
        }

        @Override
        public void tick() {
            Player owner = this.golem.level().getPlayerByUUID(this.golem.getOwnerUuid());
            if (owner == null) return;
            this.golem.getLookControl().setLookAt(owner, 10.0F, 10.0F);
            if (--this.recalc <= 0) {
                this.recalc = 10;
                this.golem.getNavigation().moveTo(owner, this.speed);
            }
            // se mantiene pegado al dueño y nunca le ataca
            this.golem.setTarget(null);
        }
    }
}
