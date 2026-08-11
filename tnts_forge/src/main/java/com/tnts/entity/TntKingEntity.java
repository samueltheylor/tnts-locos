package com.tnts.entity;

import com.tnts.ModItems;
import com.tnts.ModSounds;
import com.tnts.ModBlocks;
import com.tnts.config.TntsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * EL REY TNT: el jefe del mod que vive en el bunker de TNT abandonado.
 * <p>
 * Fases: lanza TNTs encendidas al jugador (mini, mega, rapida, lava...),
 * planta minas en el suelo y, por debajo del 50% de vida, entra en MODO
 * FURIA (lanza 3 TNTs de golpe y se mueve mas rapido). Al morir suelta la
 * Corona del Rey TNT y un botin de TNTs. Barra de jefe propia.
 */
public class TntKingEntity extends Monster {

    public static final int MAX_HP = 300;

    private final ServerBossEvent bossEvent =
            (ServerBossEvent) new ServerBossEvent(Component.translatable("entity.tnts.tnt_king"),
                    ServerBossEvent.BossBarColor.RED, ServerBossEvent.BossBarOverlay.PROGRESS)
                    .setDarkenScreen(false);

    private boolean enraged = false;

    public TntKingEntity(EntityType<? extends TntKingEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.xpReward = 200;
    }

    public boolean isEnraged() {
        return this.enraged;
    }

    // ---------- atributos y AI ----------

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HP)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.26)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    /** Registro de atributos (mod bus). */
    public static void onAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(TntsEntities.TNT_KING.get(), createAttributes().build());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TntKingAttackGoal(this));
        this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 0.3, 20.0F));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.12));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData(); // LivingEntity define sus flags aqui (obligatorio)
    }

    // ---------- tick: barra de jefe + modo furia ----------

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;

        // barra de jefe para los jugadores cercanos
        if (this.level() instanceof ServerLevel serverLevel) {
            this.bossEvent.setProgress(Math.max(0.0F, this.getHealth() / this.getMaxHealth()));
            List<ServerPlayer> near = serverLevel.getEntitiesOfClass(
                    ServerPlayer.class, new AABB(this.blockPosition()).inflate(48));
            for (ServerPlayer p : near) {
                if (!this.bossEvent.getPlayers().contains(p)) this.bossEvent.addPlayer(p);
            }
            for (ServerPlayer p : new ArrayList<>(this.bossEvent.getPlayers())) {
                if (!near.contains(p)) this.bossEvent.removePlayer(p);
            }
        }

        // modo furia por debajo del 50% de vida
        if (!this.enraged && this.getHealth() < this.getMaxHealth() / 2.0F) {
            this.enraged = true;
            if (this.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
                this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.38);
            }
            // rugido + anillo de polvo rojo
            this.level().playSound(null, this.blockPosition(), ModSounds.KING_ROAR.get(),
                    SoundSource.HOSTILE, 3.0F, 0.7F);
            if (this.level() instanceof ServerLevel sl) {
                for (int i = 0; i < 48; i++) {
                    double a = i / 48.0 * Math.PI * 2;
                    sl.sendParticles(new DustParticleOptions(new Vector3f(1.0F, 0.2F, 0.1F), 1.4F),
                            this.getX() + Math.cos(a) * 2.5, this.getY() + 0.5,
                            this.getZ() + Math.sin(a) * 2.5, 2, 0.1, 0.3, 0.1, 0.02);
                }
            }
        }
    }

    // ---------- muerte: botin del jefe ----------

    @Override
    public void die(DamageSource source) {
        super.die(source);
        this.bossEvent.removeAllPlayers();
        if (this.level() instanceof ServerLevel sl) {
            // Corona del Rey (siempre) + TNTs de botin
            this.spawnAtLocation(new ItemStack(ModItems.TNT_KING_CROWN.get()));
            for (int i = 0; i < 3 + this.random.nextInt(4); i++) {
                this.spawnAtLocation(new ItemStack(randomKingTnt()));
            }
            // experiencia
            int total = 200;
            while (total > 0) {
                int value = Math.min(total, 50);
                total -= value;
                sl.addFreshEntity(new ExperienceOrb(sl, this.getX(), this.getY() + 0.5, this.getZ(), value));
            }
            // boom de muerte (pequeno, no destructivo)
            sl.explode(this, this.getX(), this.getY(), this.getZ(),
                    2.0F, false, Level.ExplosionInteraction.NONE);
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                    this.getX(), this.getY() + 1, this.getZ(), 1, 0, 0, 0, 0);
        }
    }

    private net.minecraft.world.item.Item randomKingTnt() {
        return switch (this.random.nextInt(8)) {
            case 0 -> ModItems.MINI_TNT.get();
            case 1, 2 -> ModItems.MEGA_TNT.get();
            case 3 -> ModItems.RAPIDA_TNT.get();
            case 4 -> ModItems.LAVA_TNT.get();
            case 5 -> ModItems.RAYO_TNT.get();
            case 6 -> ModItems.ORO_TNT.get();
            default -> ModItems.DIAMANTE_TNT.get();
        };
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (!this.level().isClientSide) this.bossEvent.removeAllPlayers();
        super.remove(reason);
    }

    // ---------- sonidos / inmunidades ----------

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.KING_ROAR.get();
    }

    @Override
    protected float getSoundVolume() {
        return 2.5F;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.enraged = tag.getBoolean("KingEnraged");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("KingEnraged", this.enraged);
    }

    // ---------- ataque del jefe ----------

    /**
     * Goal de ataque: lanza TNTs encendidas al objetivo con arco, y cada
     * cierto numero de ataques planta minas alrededor de el. En modo furia
     * lanza 3 TNTs de golpe y con mucha mas frecuencia.
     */
    static class TntKingAttackGoal extends Goal {

        private final TntKingEntity king;
        private int cooldown = 50;

        TntKingAttackGoal(TntKingEntity king) {
            this.king = king;
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.king.getTarget();
            return target != null && target.isAlive()
                    && this.king.distanceToSqr(target) < 32.0 * 32.0;
        }

        @Override
        public void start() {
            this.cooldown = 40;
        }

        @Override
        public void tick() {
            LivingEntity target = this.king.getTarget();
            if (target == null) return;
            this.king.getLookControl().setLookAt(target, 30.0F, 30.0F);
            if (--this.cooldown <= 0) {
                attack(target);
                this.cooldown = this.king.enraged ? 16 : 34;
            }
        }

        private void attack(LivingEntity target) {
            int shots = this.king.enraged ? 3 : 1;
            for (int i = 0; i < shots; i++) {
                String variant = pickVariant();
                int fuse = TntsConfig.get(variant) != null ? TntsConfig.get(variant).fuse() : 40;
                TntsPrimedTnt tnt = new TntsPrimedTnt(this.king.level(),
                        this.king.getX(), this.king.getY() + 1.8, this.king.getZ(),
                        "tnts:" + variant, fuse, this.king);
                tnt.setStationary(false);
                Vec3 dir = target.position().add(0, 1.0, 0).subtract(tnt.position());
                double dist = Math.max(1.0, dir.horizontalDistance());
                tnt.setDeltaMovement(dir.normalize().scale(1.05)
                        .add(0, 0.38 + dist * 0.025, 0));
                this.king.level().addFreshEntity(tnt);
            }
            // de vez en cuando planta minas alrededor del objetivo
            if (this.king.random.nextInt(3) == 0) {
                plantMines(target);
            }
        }

        private String pickVariant() {
            int roll = this.king.random.nextInt(100);
            if (roll < 30) return "mini_tnt";
            if (roll < 55) return "mega_tnt";
            if (roll < 70) return "rapida_tnt";
            if (roll < 82) return "lava_tnt";
            if (roll < 92) return "rayo_tnt";
            return "oro_tnt";
        }

        private void plantMines(LivingEntity target) {
            Level level = this.king.level();
            if (level.isClientSide) return;
            int n = 2 + this.king.random.nextInt(2);
            for (int i = 0; i < n; i++) {
                double a = this.king.random.nextDouble() * Math.PI * 2;
                double r = 2.0 + this.king.random.nextDouble() * 2.0;
                BlockPos bp = BlockPos.containing(
                        target.getX() + Math.cos(a) * r, target.getY(), target.getZ() + Math.sin(a) * r);
                // bajar hasta el suelo
                while (bp.getY() > level.getMinBuildHeight() && level.isEmptyBlock(bp)) {
                    bp = bp.below();
                }
                bp = bp.above();
                if (level.isEmptyBlock(bp) && level.getBlockState(bp.below()).isSolid()
                        && ModBlocks.MINA_TNT.get().defaultBlockState().canSurvive(level, bp)) {
                    level.setBlock(bp, ModBlocks.MINA_TNT.get().defaultBlockState(), 3);
                }
            }
        }
    }
}
