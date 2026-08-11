package com.tnts;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
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
     */
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity == null || entity.level().isClientSide) return;
        if (!entity.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.TNT_KING_CROWN.get())) return;
        if (!entity.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 0, false, false));
        }
    }

    /**
     * Peto de TNT: al recibir dano, empuja al atacante lejos con un destello
     * de llamas y un pitido.
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity wearer = event.getEntity();
        if (wearer == null || wearer.level().isClientSide) return;
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
