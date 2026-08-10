package com.tnts.item;

import com.tnts.ModSounds;
import com.tnts.ModTriggers;
import com.tnts.block.TntBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

/**
 * Detonador remoto: al usarlo enciende todas las TNTs del mod en un radio
 * de 24 bloques alrededor del jugador. Solo revisa bloques de chunks cargados.
 *
 * Feedback: cada TNT encendida suelta un destello de particulas, y si se
 * detonan 10 o mas de golpe suena una rafaga especial con un anillo de
 * particulas rojas alrededor del jugador.
 */
public class DetonatorItem extends Item {

    private static final int RADIUS = 24;
    private static final int BURST_THRESHOLD = 10;

    public DetonatorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) return InteractionResultHolder.success(player.getItemInHand(hand));

        BlockPos center = player.blockPosition();
        int count = 0;
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-RADIUS, -8, -RADIUS),
                center.offset(RADIUS, 8, RADIUS))) {
            if (!level.isLoaded(pos)) continue; // saltar chunks sin cargar
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof TntBlock tntBlock) {
                tntBlock.prime(level, pos, state, player);
                count++;
                // destello en cada TNT encendida
                if (level instanceof ServerLevel serverLevel) {
                    double px = pos.getX() + 0.5;
                    double py = pos.getY() + 0.5;
                    double pz = pos.getZ() + 0.5;
                    serverLevel.sendParticles(ParticleTypes.FLASH, px, py, pz, 1, 0, 0, 0, 0);
                    serverLevel.sendParticles(ParticleTypes.END_ROD, px, py, pz, 5, 0.3, 0.3, 0.3, 0.05);
                }
            }
        }

        ItemStack stack = player.getItemInHand(hand);
        if (count > 0) {
            stack.hurtAndBreak(1, player, (living) -> living.broadcastBreakEvent(hand));
            if (count >= BURST_THRESHOLD) {
                // detonacion masiva: rafaga + anillo de polvo rojo + titulo con contador
                level.playSound(null, center, ModSounds.DETONATOR_BURST.get(), SoundSource.PLAYERS, 1.2F, 1.0F);
                if (player instanceof ServerPlayer serverPlayer) {
                    ModTriggers.MASS_DETONATED.trigger(serverPlayer);
                    serverPlayer.connection.send(new ClientboundSetTitlesAnimationPacket(5, 40, 10));
                    serverPlayer.connection.send(new ClientboundSetSubtitleTextPacket(
                            Component.translatable("tnts.subtitle.mass_detonation", count)));
                    serverPlayer.connection.send(new ClientboundSetTitleTextPacket(
                            Component.translatable("tnts.title.mass_detonation")));
                }
                if (level instanceof ServerLevel serverLevel) {
                    double px = player.getX();
                    double py = player.getY() + 1.0;
                    double pz = player.getZ();
                    for (int i = 0; i < 24; i++) {
                        double angle = i / 24.0 * Math.PI * 2;
                        serverLevel.sendParticles(
                                new DustParticleOptions(new Vector3f(0.9F, 0.15F, 0.15F), 1.0F),
                                px + Math.cos(angle) * 1.5, py, pz + Math.sin(angle) * 1.5,
                                1, 0, 0, 0, 0);
                    }
                }
            } else {
                level.playSound(null, center, ModSounds.DETONATOR_CLICK.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            player.getCooldowns().addCooldown(this, 10);
        } else {
            level.playSound(null, center, ModSounds.DETONATOR_CLICK.get(), SoundSource.PLAYERS, 0.5F, 0.5F);
        }
        return InteractionResultHolder.success(stack);
    }
}
