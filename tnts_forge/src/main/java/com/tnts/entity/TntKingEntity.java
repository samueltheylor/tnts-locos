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

    // ---- embestida (modo furia) ----
    private int chargeCooldown = 0;   // ticks hasta poder cargar otra vez
    private int chargingTicks = 0;    // >0 = aviso de 3s antes de la embestida
    private boolean dashing = false;  // true = surcando hacia el objetivo
    private int dashTicks = 0;
    private Vec3 dashDir = Vec3.ZERO;

    // ---- aturdimiento (desactivar su TNT Real con tijeras) ----
    private int stunTicks = 0;

    // ---- invocacion de piglins ----
    private int summonCooldown = 100; // primera invocacion a los 5s

    public TntKingEntity(EntityType<? extends TntKingEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.xpReward = 200;
    }

    public boolean isEnraged() {
        return this.enraged;
    }

    /**
     * Fases visuales: cuantas mas grietas tiene el Rey (0 = sano, 1 = agrietado,
     * 2 = muy agrietado) segun su vida. El renderer elige la textura con esta
     * escala, y el tick suelta fragmentos de bloque cuanto mas bajo esta.
     */
    public int getCrackLevel() {
        if (this.dead) return 2; // en la derrota ya se agrieta entero
        float ratio = this.getHealth() / this.getMaxHealth();
        if (ratio <= 1.0F / 3.0F) return 2;
        if (ratio <= 2.0F / 3.0F) return 1;
        return 0;
    }

    public boolean isStunned() {
        return this.stunTicks > 0;
    }

    public boolean isCharging() {
        return this.chargingTicks > 0;
    }

    public boolean isDashing() {
        return this.dashing;
    }

    /** Aturde al Rey (desactivar una TNT Real con tijeras lo deja 5s inmobil). */
    public void setStunned(int ticks) {
        this.stunTicks = Math.max(this.stunTicks, ticks);
        this.getNavigation().stop();
    }

    /** Empieza el aviso de 3 segundos antes de la embestida. */
    public void startCharge() {
        if (this.chargingTicks > 0 || this.dashing) return;
        this.chargingTicks = 60; // 3 segundos de aviso
        this.getNavigation().stop();
        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.blockPosition(), ModSounds.KING_CHARGE.get(),
                    SoundSource.HOSTILE, 2.5F, 1.0F);
        }
    }

    /** Invoca piglins del Nether (usado por el cooldown y por los GameTests). */
    public void forceSummon() {
        summonPiglins();
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
        // muerto: solo corre la secuencia de derrota (tickDeath), nada mas
        if (this.dead) return;

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

        // ==================== FASES VISUALES (1.10.9) ====================
        // cuanto menos vida, mas se agrieta: suelta fragmentos de su propio
        // bloque con mas frecuencia segun el nivel de grietas
        int cracks = this.getCrackLevel();
        if (cracks > 0 && this.level() instanceof ServerLevel sl) {
            int interval = cracks == 2 ? 5 : 12;
            if (this.tickCount % interval == 0) {
                int n = cracks == 2 ? 3 : 1;
                for (int i = 0; i < n; i++) {
                    sl.sendParticles(new net.minecraft.core.particles.BlockParticleOption(
                                    ParticleTypes.BLOCK, ModBlocks.MEGA_TNT.get().defaultBlockState()),
                            this.getX() + (this.random.nextDouble() - 0.5) * 1.6,
                            this.getY() + this.random.nextDouble() * 2.2,
                            this.getZ() + (this.random.nextDouble() - 0.5) * 1.6,
                            2, 0.2, 0.3, 0.2, 0.0);
                }
                // humo de la mecha cada vez mas denso con el daño
                sl.sendParticles(ParticleTypes.SMOKE,
                        this.getX(), this.getY() + 2.1, this.getZ(),
                        cracks, 0.3, 0.1, 0.3, 0.0);
            }
        }

        // ==================== PELEA MEJORADA 1.10.1 ====================

        // --- aturdimiento: inmobil (sin movimiento horizontal) + estrellitas ---
        if (this.stunTicks > 0) {
            this.stunTicks--;
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.0, 1.0, 0.0));
            this.getNavigation().stop();
            if (this.level() instanceof ServerLevel sl && this.stunTicks % 4 == 0) {
                for (int i = 0; i < 3; i++) {
                    double a = (this.tickCount + i * 2) * 0.45;
                    sl.sendParticles(new DustParticleOptions(
                                    new Vector3f(1.0F, 0.85F, 0.1F), 1.0F),
                            this.getX() + Math.cos(a) * 0.7, this.getY() + 1.6,
                            this.getZ() + Math.sin(a) * 0.7, 1, 0, 0, 0, 0);
                }
            }
        }

        // --- invocacion de piglins del Nether (cada ~20s con objetivo) ---
        if (this.summonCooldown > 0) this.summonCooldown--;
        if (this.summonCooldown == 0 && this.getTarget() != null) {
            summonPiglins();
            this.summonCooldown = 400; // 20 segundos
        }

        // --- embestida: aviso de 3s (60 ticks) y luego dash ---
        if (this.chargeCooldown > 0) this.chargeCooldown--;
        if (this.chargingTicks > 0) {
            this.chargingTicks--;
            // aviso: polvo rojo que se acumula + sacudida + gemido que sube
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.3, 1.0, 0.3));
            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(new DustParticleOptions(
                                new Vector3f(1.0F, 0.25F, 0.1F), 1.4F),
                        this.getX() + (this.random.nextDouble() - 0.5) * 1.6,
                        this.getY() + this.random.nextDouble() * 2.2,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 1.6,
                        3, 0.1, 0.1, 0.1, 0.02);
                if (this.chargingTicks % 12 == 0) {
                    sl.playSound(null, this.blockPosition(), ModSounds.KING_CHARGE.get(),
                            SoundSource.HOSTILE, 2.0F, 0.8F + (60 - this.chargingTicks) / 60.0F * 0.4F);
                }
            }
            if (this.chargingTicks <= 0) {
                // !!! EMBESTIDA !!!
                this.dashing = true;
                this.dashTicks = 18;
                LivingEntity target = this.getTarget();
                Vec3 dir = target != null
                        ? target.position().subtract(this.position())
                        : this.getLookAngle();
                this.dashDir = new Vec3(dir.x, 0, dir.z).normalize();
            }
        }
        if (this.dashing) {
            this.dashTicks--;
            this.setDeltaMovement(this.dashDir.scale(1.7).add(0, this.getDeltaMovement().y, 0));
            this.hasImpulse = true;
            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5,
                        this.getZ(), 3, 0.3, 0.4, 0.3, 0.02);
                sl.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 0.5,
                        this.getZ(), 1, 0.2, 0.3, 0.2, 0.0);
                // dano y knockback a quien toque
                AABB hit = this.getBoundingBox().inflate(1.3);
                for (LivingEntity e : sl.getEntitiesOfClass(
                        LivingEntity.class, hit, e -> e != this && !(e instanceof TntKingEntity))) {
                    e.hurt(e.damageSources().mobAttack(this), 12.0F);
                    Vec3 away = e.position().subtract(this.position());
                    if (away.horizontalDistanceSqr() < 0.001) away = new Vec3(1, 0, 0);
                    Vec3 n = away.normalize();
                    e.push(n.x * 1.8, 0.7, n.z * 1.8);
                    e.hurtMarked = true;
                }
            }
            if (this.dashTicks <= 0) {
                this.dashing = false;
                this.chargeCooldown = 200; // 10 segundos hasta la siguiente
            }
        }
    }

    /**
     * Grito del Rey: invoca piglins brutos y piglins zombificados del Nether
     * alrededor, con particulas de portal, que atacan a su objetivo.
     */
    private void summonPiglins() {
        if (this.level().isClientSide || !(this.level() instanceof ServerLevel sl)) return;
        sl.playSound(null, this.blockPosition(), ModSounds.KING_ROAR.get(),
                SoundSource.HOSTILE, 3.0F, 0.55F);
        int count = 2 + this.random.nextInt(3);
        for (int i = 0; i < count; i++) {
            double a = this.random.nextDouble() * Math.PI * 2;
            double r = 2.0 + this.random.nextDouble() * 3.0;
            BlockPos spawn = BlockPos.containing(
                    this.getX() + Math.cos(a) * r, this.getY(), this.getZ() + Math.sin(a) * r);
            while (spawn.getY() > sl.getMinBuildHeight() && sl.isEmptyBlock(spawn)) {
                spawn = spawn.below();
            }
            spawn = spawn.above();
            net.minecraft.world.entity.Mob mob = this.random.nextInt(3) == 0
                    ? new net.minecraft.world.entity.monster.piglin.PiglinBrute(
                            net.minecraft.world.entity.EntityType.PIGLIN_BRUTE, sl)
                    : new net.minecraft.world.entity.monster.ZombifiedPiglin(
                            net.minecraft.world.entity.EntityType.ZOMBIFIED_PIGLIN, sl);
            mob.moveTo(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, 0, 0);
            if (this.getTarget() != null) mob.setTarget(this.getTarget());
            sl.addFreshEntity(mob);
            sl.sendParticles(ParticleTypes.PORTAL,
                    spawn.getX() + 0.5, spawn.getY() + 1, spawn.getZ() + 0.5,
                    20, 0.4, 0.6, 0.4, 0.1);
        }
    }

    // ---------- muerte: secuencia de derrota + botin ----------

    /** Parpadeo blanco activo? (derrota: brilla cada 3 ticks durante 2s). */
    public boolean isDeathFlashOn() {
        return this.dead && (this.deathTime / 3) % 2 == 0;
    }

    /** Esta en la secuencia de derrota? (muerto, aun no removido). */
    public boolean isDying() {
        return this.dead;
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        this.bossEvent.removeAllPlayers();
        // el boom real se dispara al final de la secuencia de derrota
        // (tickDeath), cuando se agrieta entero. Aqui solo el botin.
        if (this.level() instanceof ServerLevel sl) {
            // Corona del Rey (siempre) + TNTs de botin
            this.spawnAtLocation(new ItemStack(ModItems.TNT_KING_CROWN.get()));
            for (int i = 0; i < 3 + this.random.nextInt(4); i++) {
                this.spawnAtLocation(new ItemStack(randomKingTnt()));
            }
            // botin raro: espada (40%) y escudo (40%) del Rey
            if (this.random.nextFloat() < 0.4F) {
                this.spawnAtLocation(new ItemStack(ModItems.TNT_KING_SWORD.get()));
            }
            if (this.random.nextFloat() < 0.4F) {
                this.spawnAtLocation(new ItemStack(ModItems.TNT_SHIELD.get()));
            }
            // experiencia
            int total = 200;
            while (total > 0) {
                int value = Math.min(total, 50);
                total -= value;
                sl.addFreshEntity(new ExperienceOrb(sl, this.getX(), this.getY() + 0.5, this.getZ(), value));
            }
        }
    }

    /**
     * Secuencia de derrota (2.2s): el Rey se queda quieto, parpadea en
     * blanco, suelta fragmentos de bloque (se agrieta), tiembla y al final
     * hace BOOM con su propia explosion.
     */
    @Override
    protected void tickDeath() {
        ++this.deathTime;
        if (this.level().isClientSide) return;
        ServerLevel sl = (ServerLevel) this.level();

        // grietas: fragmentos de su propio bloque saltando (mas con el tiempo)
        int cracks = 2 + this.deathTime / 8;
        for (int i = 0; i < cracks; i++) {
            sl.sendParticles(new net.minecraft.core.particles.BlockParticleOption(
                            ParticleTypes.BLOCK, ModBlocks.MEGA_TNT.get().defaultBlockState()),
                    this.getX() + (this.random.nextDouble() - 0.5) * 1.6,
                    this.getY() + this.random.nextDouble() * 2.2,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 1.6,
                    2, 0.2, 0.3, 0.2, 0.0);
        }
        // humo y chispas de la mecha cada vez mas densos
        sl.sendParticles(ParticleTypes.SMOKE,
                this.getX(), this.getY() + 2.1, this.getZ(),
                com.tnts.config.TntsConfig.particles(2), 0.3, 0.1, 0.3, 0.0);
        sl.sendParticles(ParticleTypes.SMALL_FLAME,
                this.getX(), this.getY() + 2.2, this.getZ(),
                com.tnts.config.TntsConfig.particles(1), 0.2, 0.1, 0.2, 0.01);
        // temblor de pantalla para los jugadores cerca (cada 6 ticks)
        if (this.deathTime % 6 == 0) {
            AABB camBox = new AABB(this.blockPosition()).inflate(32);
            for (ServerPlayer sp : sl.getEntitiesOfClass(ServerPlayer.class, camBox)) {
                sp.animateHurt(sp.getYRot());
            }
        }

        if (this.deathTime >= 44) {
            // ¡BOOM final! explota de verdad y desaparece
            sl.explode(this, this.getX(), this.getY(), this.getZ(),
                    5.0F, true, Level.ExplosionInteraction.BLOCK);
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                    this.getX(), this.getY() + 1, this.getZ(), 1, 0, 0, 0, 0);
            sl.sendParticles(ParticleTypes.FLASH,
                    this.getX(), this.getY() + 1, this.getZ(), 3, 0, 0, 0, 0);
            sl.sendParticles(ParticleTypes.END_ROD,
                    this.getX(), this.getY() + 1, this.getZ(),
                    40, 4, 2, 4, 0.2);
            sl.playSound(null, this.blockPosition(), net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE,
                    net.minecraft.sounds.SoundSource.HOSTILE, 4.0F, 0.8F);
            this.remove(Entity.RemovalReason.KILLED);
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
        this.stunTicks = tag.getInt("KingStunTicks");
        this.chargeCooldown = tag.getInt("KingChargeCooldown");
        this.summonCooldown = tag.getInt("KingSummonCooldown");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("KingEnraged", this.enraged);
        tag.putInt("KingStunTicks", this.stunTicks);
        tag.putInt("KingChargeCooldown", this.chargeCooldown);
        tag.putInt("KingSummonCooldown", this.summonCooldown);
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

        private int attacks = 0; // para alternar la TNT Real en furia

        @Override
        public boolean canUse() {
            LivingEntity target = this.king.getTarget();
            return target != null && target.isAlive()
                    && this.king.distanceToSqr(target) < 32.0 * 32.0
                    && !this.king.isStunned()
                    && !this.king.isCharging()
                    && !this.king.isDashing();
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

            // en modo furia, cada cierto tiempo carga la EMBESTIDA
            // (3 segundos de aviso con polvo rojo antes de salir disparado)
            if (this.king.enraged && this.king.chargeCooldown == 0) {
                this.king.startCharge();
                this.cooldown = Math.max(this.cooldown, 45);
                return;
            }

            if (--this.cooldown <= 0) {
                attack(target);
                this.attacks++;
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

            // en modo furia, cada 3 ataque tambien lanza 2 TNTs REALES:
            // mecha corta, radio enorme, y SOLO se frenan desactivandolas
            // con tijeras (eso aturde al Rey 5 segundos -> ventana de dano)
            if (this.king.enraged && this.attacks % 3 == 0) {
                throwRoyalTnts(target);
            }

            // de vez en cuando planta minas alrededor del objetivo
            if (this.king.random.nextInt(3) == 0) {
                plantMines(target);
            }
        }

        private void throwRoyalTnts(LivingEntity target) {
            for (int i = 0; i < 2; i++) {
                TntsPrimedTnt tnt = new TntsPrimedTnt(this.king.level(),
                        this.king.getX(), this.king.getY() + 2.0, this.king.getZ(),
                        "tnts:mega_tnt", 35, this.king);
                tnt.setStationary(false);
                tnt.setRoyal(true);
                Vec3 to = target.position().add(0, 0.8, 0).subtract(tnt.position());
                double dist = Math.max(1.0, to.horizontalDistance());
                double spread = (i == 0 ? -0.25 : 0.25);
                Vec3 dir = to.normalize();
                tnt.setDeltaMovement(
                        new Vec3(dir.x + spread, 0, dir.z + spread).normalize()
                                .scale(0.95).add(0, 0.45 + dist * 0.02, 0));
                this.king.level().addFreshEntity(tnt);
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
