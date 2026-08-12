package com.tnts;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Eventos del mod (registrados en el bus de eventos de Forge). */
public class TntsEvents {

    /** Trades del aldeano Experto en TNTs. */
    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        ModVillagers.registerTrades(event);
    }

    /**
     * Corona del Rey TNT: mientras la llevas puesta tienes Resistencia al
     * Fuego permanente (se reaplica al terminar).
     * <p>
     * Set bonus (4 piezas): aura del Rey — cada 2s enciende las TNTs del
     * mod cercanas automaticamente con un destello dorado.
     */
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity == null || entity.level().isClientSide) return;

        if (entity.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.TNT_KING_CROWN.get())) {
            if (!entity.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 0, false, false));
            }
        }

        // Casco de TNT: vision nocturna mientras lo llevas puesto
        if (entity.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.TNT_HELMET.get())) {
            if (!entity.hasEffect(MobEffects.NIGHT_VISION)) {
                entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 220, 0, false, false));
            }
        }

        // Botas de TNT: impulso de explosion al agacharte estando en el aire
        // (doble salto con mini-explosion; cooldown de 10 ticks)
        if (entity.getItemBySlot(EquipmentSlot.FEET).is(ModItems.TNT_BOOTS.get())
                && entity instanceof net.minecraft.world.entity.player.Player player
                && !player.onGround()
                && player.isShiftKeyDown()
                && player.getDeltaMovement().y < 0.4
                && player.tickCount % 10 == 0
                && entity.level() instanceof ServerLevel serverLevel) {
            if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
                ModTriggers.DOUBLE_JUMPED.trigger(sp);
            }
            player.setDeltaMovement(player.getDeltaMovement().multiply(1, 0, 1)
                    .add(0, 0.85, 0));
            player.hurtMarked = true;
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    player.getX(), player.getY() + 0.2, player.getZ(),
                    10, 0.3, 0.1, 0.3, 0.05);
            serverLevel.sendParticles(ParticleTypes.SMALL_FLAME,
                    player.getX(), player.getY() + 0.2, player.getZ(),
                    4, 0.3, 0.1, 0.3, 0.02);
            serverLevel.playSound(null, player.blockPosition(),
                    net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE,
                    SoundSource.PLAYERS, 0.7F, 1.6F);
            player.getItemBySlot(EquipmentSlot.FEET)
                    .hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(EquipmentSlot.FEET));
        }

        // SET BONUS DEL REY: dispara los advancements segun cuantas piezas lleva
        if (entity instanceof net.minecraft.server.level.ServerPlayer sp && !sp.level().isClientSide) {
            int pieces = TntKingSet.countPieces(entity);
            if (pieces >= 2) ModTriggers.KING_SET.trigger(sp, pieces);
        }

        // AURA DEL REY (4 piezas): enciende las TNTs cercanas cada 40 ticks
        if (TntKingSet.countPieces(entity) >= 4
                && entity.tickCount % 40 == 0
                && entity.level() instanceof ServerLevel serverLevel) {
            BlockPos center = entity.blockPosition();
            int primed = 0;
            for (BlockPos p : BlockPos.betweenClosed(
                    center.offset(-8, -4, -8), center.offset(8, 4, 8))) {
                BlockState state = serverLevel.getBlockState(p);
                if (state.getBlock() instanceof com.tnts.block.TntBlock tnt
                        && !state.getValue(com.tnts.block.TntBlock.LIT)) {
                    tnt.prime(serverLevel, p, state,
                            entity instanceof net.minecraft.world.entity.player.Player pl ? pl : null);
                    // si la mecha se encendio, el bloque ya no es una TNT apagada
                    if (!(serverLevel.getBlockState(p).getBlock() instanceof com.tnts.block.TntBlock)) {
                        primed++;
                    }
                }
            }
            if (primed > 0) {
                // destello dorado del aura
                for (int i = 0; i < 24; i++) {
                    double a = entity.level().random.nextDouble() * Math.PI * 2;
                    double r = 1.0 + entity.level().random.nextDouble() * 2.5;
                    serverLevel.sendParticles(new net.minecraft.core.particles.DustParticleOptions(
                                    new org.joml.Vector3f(1.0F, 0.8F, 0.1F), 1.0F),
                            entity.getX() + Math.cos(a) * r, entity.getY() + 1.0,
                            entity.getZ() + Math.sin(a) * r, 1, 0, 0.3, 0, 0);
                }
                serverLevel.playSound(null, entity.blockPosition(), ModSounds.BEEP.get(),
                        SoundSource.PLAYERS, 0.8F, 1.3F);
            }
        }
    }

    /**
     * Peto de TNT: al recibir dano, empuja al atacante lejos con un destello
     * de llamas y un pitido.
     * <p>
     * Set bonus (3 piezas): "Escudo Real" — inmunidad al dano de las TNTs
     * del propio mod (las explosiones propias ya no te hieren).
     * <p>
     * Armadura de TNT (4 piezas): "Blindaje de TNT" — el daño de cualquier
     * explosion se reduce a la mitad.
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity wearer = event.getEntity();
        if (wearer == null || wearer.level().isClientSide) return;

        // SET BONUS (3 piezas): inmunidad a explosiones de TNTs del mod
        if (TntKingSet.countPieces(wearer) >= 3
                && event.getSource().is(net.minecraft.world.damagesource.DamageTypes.EXPLOSION)) {
            Entity direct = event.getSource().getDirectEntity();
            // solo las explosiones de nuestras TNTs (entidades del mod)
            if (direct != null && direct.getType().getDescriptionId().startsWith("entity.tnts.")) {
                event.setCanceled(true);
                return;
            }
        }

        // ARMADURA DE TNT (4 piezas): +50% resistencia a explosiones
        if (TntArmorSet.isComplete(wearer)
                && event.getSource().is(net.minecraft.world.damagesource.DamageTypes.EXPLOSION)) {
            event.setAmount(event.getAmount() * 0.5F);
        }

        if (!wearer.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.TNT_CHESTPLATE.get())) return;

        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof LivingEntity livingAttacker)) return;

        Vec3 away = wearer.position().subtract(attacker.position());
        if (away.horizontalDistance() < 0.001) return;
        Vec3 dir = away.normalize();
        livingAttacker.push(dir.x * 1.6, 0.8, dir.z * 1.6);
        livingAttacker.hurtMarked = true;

        if (wearer.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    attacker.getX(), attacker.getY() + 1, attacker.getZ(),
                    10, 0.3, 0.3, 0.3, 0.02);
            serverLevel.playSound(null, attacker.blockPosition(), ModSounds.BEEP.get(),
                    SoundSource.PLAYERS, 0.5F, 0.7F);
        }
    }

    /**
     * Escudo de TNT: al bloquear un golpe con el escudo, empuja al atacante
     * lejos con un destello de llamas y un sonido de explosion pequeña.
     */
    @SubscribeEvent
    public static void onShieldBlock(ShieldBlockEvent event) {
        LivingEntity wearer = event.getEntity();
        if (wearer == null || wearer.level().isClientSide) return;
        if (!wearer.getUseItem().is(ModItems.TNT_SHIELD.get())) return;

        Entity attacker = event.getDamageSource().getEntity();
        if (!(attacker instanceof LivingEntity livingAttacker)) return;

        Vec3 away = wearer.position().subtract(attacker.position());
        if (away.horizontalDistance() < 0.001) return;
        Vec3 dir = away.normalize();
        livingAttacker.push(dir.x * 2.0, 0.9, dir.z * 2.0);
        livingAttacker.hurtMarked = true;

        if (wearer.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    attacker.getX(), attacker.getY() + 1, attacker.getZ(),
                    12, 0.3, 0.3, 0.3, 0.02);
            serverLevel.sendParticles(ParticleTypes.SMOKE,
                    attacker.getX(), attacker.getY() + 1, attacker.getZ(),
                    8, 0.3, 0.3, 0.3, 0.01);
            serverLevel.playSound(null, attacker.blockPosition(),
                    net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE,
                    SoundSource.PLAYERS, 0.6F, 1.4F);
        }
    }
}
