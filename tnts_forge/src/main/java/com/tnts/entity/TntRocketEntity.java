package com.tnts.entity;

import com.tnts.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;

import java.util.List;

/**
 * TNT MONTABLE (TNT Cohete): una TNT que puedes montar y que despega como un
 * cohete al agacharte.
 * <p>
 * Mecanicas:
 * <ul>
 *   <li>Click derecho (o el item "TNT Cohete") -> te montas encima.</li>
 *   <li>AGACHARTE (sneak) -> despega: empuje vertical + horizontal hacia donde
 *       miras, con estela de fuego, humo y llamas.</li>
 *   <li>AGACHARTE DE NUEVO mientras vuela -> frena el impulso y desciende
 *       suavemente (paracaidas de humo).</li>
 *   <li>Al aterrizar despues de volar, o al romperse, explota con una pequena
 *       explosion (radio 3, sin romper bloques si el jugador va encima).</li>
 * </ul>
 */
public class TntRocketEntity extends Entity {

    /** Estado: 0 = en el suelo, 1 = despegando, 2 = volando, 3 = frenando. */
    private int state = 0;
    private int stateTicks = 0;
    private boolean exploded = false;

    public TntRocketEntity(EntityType<? extends TntRocketEntity> type, Level level) {
        super(type, level);
    }

    public static void onAttributeCreate(EntityAttributeCreationEvent event) {
        // sin atributos (es una entidad, no un LivingEntity)
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.state = tag.getInt("RocketState");
        this.stateTicks = tag.getInt("RocketStateTicks");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("RocketState", this.state);
        tag.putInt("RocketStateTicks", this.stateTicks);
    }

    public boolean isFlying() {
        return this.state == 2;
    }

    // ---------- interaccion: montar ----------

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (this.level().isClientSide) return InteractionResult.sidedSuccess(true);
        if (player.isSecondaryUseActive()) return InteractionResult.PASS;
        if (!this.getPassengers().isEmpty()) return InteractionResult.PASS;
        player.startRiding(this);
        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.blockPosition(), net.minecraft.sounds.SoundEvents.WOOD_PLACE,
                    SoundSource.NEUTRAL, 0.8F, 1.2F);
        }
        return InteractionResult.sidedSuccess(true);
    }

    @Override
    protected void addPassenger(Entity passenger) {
        super.addPassenger(passenger);
        if (this.level() instanceof ServerLevel sl) {
            // sincroniza el estado (que no este volando cuando se montan)
            sl.getChunkSource().broadcastAndSend(this,
                    new net.minecraft.network.protocol.game.ClientboundSetPassengersPacket(this));
        }
    }

    // ---------- tick: fisica del cohete ----------

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        ServerLevel sl = (ServerLevel) this.level();

        // gravedad cuando no vuela
        if (this.state != 2) {
            if (!this.onGround()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0, -0.08, 0));
            }
        }
        this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
        // friccion en el suelo
        if (this.onGround() && this.state != 2) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.8, 0.0, 0.8));
        }

        Player rider = this.getControllingPassenger() instanceof Player p ? p : null;
        boolean sneaking = rider != null && rider.isShiftKeyDown();

        switch (this.state) {
            case 0: // en el suelo
                if (sneaking) {
                    this.state = 1;
                    this.stateTicks = 15;
                    sl.playSound(null, this.blockPosition(), ModSounds.BEEP.get(),
                            SoundSource.NEUTRAL, 1.2F, 0.5F);
                }
                break;
            case 1: // despegando (aviso de 15 ticks con chispas)
                this.stateTicks--;
                // chispas acumulandose
                sl.sendParticles(ParticleTypes.SMOKE,
                        this.getX(), this.getY() + 0.9, this.getZ(),
                        4, 0.2, 0.1, 0.2, 0.0);
                sl.sendParticles(ParticleTypes.SMALL_FLAME,
                        this.getX(), this.getY() + 0.95, this.getZ(),
                        3, 0.15, 0.05, 0.15, 0.01);
                if (this.stateTicks % 5 == 0) {
                    sl.playSound(null, this.blockPosition(), ModSounds.BEEP.get(),
                            SoundSource.NEUTRAL, 1.2F, 0.6F + (15 - this.stateTicks) * 0.03F);
                }
                if (this.stateTicks <= 0) {
                    // ¡DESPEGUE!
                    this.state = 2;
                    this.stateTicks = 0;
                    sl.playSound(null, this.blockPosition(), ModSounds.explode("mini_tnt"),
                            SoundSource.NEUTRAL, 2.0F, 0.6F);
                }
                break;
            case 2: // volando: empuje constante + estela
                this.stateTicks++;
                Vec3 look = rider != null ? rider.getLookAngle() : this.getLookAngle();
                Vec3 dir = new Vec3(look.x, Math.max(0.25, look.y * 0.6), look.z).normalize();
                // empuje hacia la direccion de la mirada + sosten en el aire
                this.setDeltaMovement(this.getDeltaMovement().add(
                        dir.x * 0.12, 0.14 + look.y * 0.05, dir.z * 0.12));
                // estela de fuego y humo densa
                sl.sendParticles(ParticleTypes.FLAME,
                        this.getX(), this.getY() + 0.4, this.getZ(),
                        com.tnts.config.TntsConfig.particles(2), 0.1, 0.0, 0.1, 0.0);
                sl.sendParticles(ParticleTypes.SMOKE,
                        this.getX(), this.getY() + 0.5, this.getZ(),
                        com.tnts.config.TntsConfig.particles(3), 0.2, 0.0, 0.2, 0.01);
                sl.sendParticles(ParticleTypes.LARGE_SMOKE,
                        this.getX(), this.getY() + 0.3, this.getZ(),
                        com.tnts.config.TntsConfig.particles(1), 0.1, 0.0, 0.1, 0.0);
                // limite de altura para no volar al cielo infinitamente
                if (this.getY() > this.level().getHeight() + 80) {
                    this.state = 3;
                    this.stateTicks = 0;
                }
                // agacharse de nuevo -> frenar y bajar. Solo cuenta despues de
                // 10 ticks de vuelo: si mantienes agachado al despegar (lo
                // normal), el cohete sigue volando hasta que sueltas y
                // reagachas.
                if (this.stateTicks > 10 && (sneaking || rider == null)) {
                    this.state = 3;
                    this.stateTicks = 0;
                }
                break;
            case 3: // frenando: cae con paracaidas de humo
                this.stateTicks++;
                sl.sendParticles(ParticleTypes.CLOUD,
                        this.getX(), this.getY() + 0.5, this.getZ(),
                        com.tnts.config.TntsConfig.particles(2), 0.3, 0.1, 0.3, 0.0);
                // cae suave (frena la caida)
                Vec3 mv = this.getDeltaMovement();
                this.setDeltaMovement(mv.x * 0.9, Math.min(mv.y, -0.15), mv.z * 0.9);
                if (this.onGround() || this.stateTicks > 200) {
                    this.state = 0;
                    this.stateTicks = 0;
                }
                break;
        }

        // aterrizaje fuerte despues de volar: pequena explosion (sin romper
        // bloques si llevas pasajero, para no matar al jugador por su culpa)
        if (this.stateTicks > 30 && this.onGround() && !this.exploded) {
            this.exploded = true;
            boolean safe = !this.getPassengers().isEmpty();
            sl.explode(this, this.getX(), this.getY(), this.getZ(),
                    3.0F, false,
                    safe ? Level.ExplosionInteraction.NONE : Level.ExplosionInteraction.BLOCK);
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                    this.getX(), this.getY() + 0.5, this.getZ(), 1, 0, 0, 0, 0);
            if (!safe) this.discard();
        }
        // limpieza: si lleva mucho rato tirado en el suelo sin volar, se va
        if (this.state == 0 && this.tickCount > 1200 && this.getPassengers().isEmpty()) {
            this.discard();
        }
    }

    // ---------- muerte / rotura ----------

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isRemoved() || this.level().isClientSide) return false;
        this.discard();
        if (this.level() instanceof ServerLevel sl) {
            sl.explode(this, this.getX(), this.getY(), this.getZ(),
                    2.0F, false, Level.ExplosionInteraction.BLOCK);
        }
        return true;
    }

    // ---------- control del pasajero ----------

    @Override
    public LivingEntity getControllingPassenger() {
        Entity e = this.getFirstPassenger();
        return e instanceof LivingEntity living ? living : null;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().isEmpty();
    }

    @Override
    public boolean isPushable() {
        return true;
    }
}
