package com.tnts;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Eventos del mod (registrados en el bus de eventos de Forge). */
public class TntsEvents {

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
}
