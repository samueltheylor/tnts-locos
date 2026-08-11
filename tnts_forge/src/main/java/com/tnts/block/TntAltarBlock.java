package com.tnts.block;

import com.tnts.ModSounds;
import com.tnts.entity.TntKingEntity;
import com.tnts.entity.TntsEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Altar del Rey TNT: el centro del bunker de TNT abandonado. Al usarlo con
 * la mano vacia convoca al Rey TNT (solo si no hay otro vivo cerca), con
 * rugido, destello y anillo de particulas de portal.
 */
public class TntAltarBlock extends Block {

    public TntAltarBlock(MapColor color) {
        super(Properties.of()
                .mapColor(color)
                .strength(3.5f, 1200.0f)
                .sound(SoundType.STONE)
                .lightLevel(state -> 7));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            // solo convoca si no hay otro Rey TNT vivo cerca
            boolean already = !serverLevel.getEntitiesOfClass(
                    TntKingEntity.class, new AABB(pos).inflate(48)).isEmpty();
            if (!already) {
                TntKingEntity king = new TntKingEntity(TntsEntities.TNT_KING.get(), serverLevel);
                king.moveTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0.0F, 0.0F);
                serverLevel.addFreshEntity(king);

                // espectaculo de invocacion: rugido + destello + anillo de portal
                serverLevel.playSound(null, pos, ModSounds.KING_ROAR.get(),
                        SoundSource.HOSTILE, 3.0F, 1.0F);
                serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                        pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 1, 0, 0, 0, 0);
                for (int i = 0; i < 40; i++) {
                    double a = i / 40.0 * Math.PI * 2;
                    serverLevel.sendParticles(ParticleTypes.PORTAL,
                            pos.getX() + 0.5 + Math.cos(a) * 1.6,
                            pos.getY() + 1.3,
                            pos.getZ() + 0.5 + Math.sin(a) * 1.6,
                            3, 0.1, 0.5, 0.1, 0.1);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
