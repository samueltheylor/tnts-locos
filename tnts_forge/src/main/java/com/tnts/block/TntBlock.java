package com.tnts.block;

import com.tnts.ModSounds;
import com.tnts.ModTriggers;
import com.tnts.config.TntsConfig;
import com.tnts.entity.TntsPrimedTnt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

/**
 * Bloque TNT personalizado. Solo se enciende con un mechero (flint & steel)
 * o carga de fuego, tambien con redstone y en cadena si una explosion la
 * alcanza. Al encenderse desaparece y spawna una entidad {@link TntsPrimedTnt}
 * que se queda CLAVADA en su sitio (no se mueve de lugar).
 * Los valores (radio, mecha, efectos) vienen de la config {@link TntsConfig}.
 */
public class TntBlock extends Block {

    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    private final String name;
    private final TntProperties defaultProps;

    public TntBlock(String name, TntProperties defaultProps, MapColor color) {
        super(Properties.of()
                .mapColor(color)
                .strength(0.2f, 0.0f)
                .sound(SoundType.GRASS));
        this.name = name;
        this.defaultProps = defaultProps;
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false));
    }

    /** Propiedades actuales (config si esta cargada, si no los valores por defecto). */
    public TntProperties getTntProperties() {
        TntProperties config = TntsConfig.get(name);
        return config != null ? config : defaultProps;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(LIT) ? 7 : 0;
    }

    /** Enciende la mecha (o no hace nada si ya esta encendida). */
    public void prime(Level level, BlockPos pos, BlockState state) {
        prime(level, pos, state, null);
    }

    public void prime(Level level, BlockPos pos, BlockState state, @Nullable Player player) {
        if (state.getValue(LIT)) return;
        if (level.isClientSide) return;
        // TNT desactivada en config: no se enciende con nada
        if (!TntsConfig.isEnabled(name)) return;

        // quitar el bloque y spawnear la entidad con fisica
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        String blockId = ForgeRegistries.BLOCKS.getKey(this).toString();
        int fuse = getTntProperties().fuse();
        TntsPrimedTnt tnt = new TntsPrimedTnt(
                level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, blockId, fuse, player);
        // con la Corona del Rey TNT puesta, tus TNTs explotan con +50% de radio
        if (player != null && player.getItemBySlot(
                net.minecraft.world.entity.EquipmentSlot.HEAD)
                .is(com.tnts.ModItems.TNT_KING_CROWN.get())) {
            tnt.setPowerMul(1.5f);
        }
        level.addFreshEntity(tnt);

        level.playSound(null, pos, ModSounds.fuse(name), SoundSource.BLOCKS, 1.0F, 1.0F);

        // nube de humo al encenderse
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    8, 0.3, 0.3, 0.3, 0.05);
        }

        // advancement: "enciende tu primera TNT"
        if (player instanceof ServerPlayer serverPlayer) {
            ModTriggers.IGNITED.trigger(serverPlayer, ForgeRegistries.BLOCKS.getKey(this));
        }
    }

    // --- Solo se enciende con mechero o carga de fuego ---
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.is(Items.FLINT_AND_STEEL) || stack.is(Items.FIRE_CHARGE)) {
                prime(level, pos, state, player);
                if (!player.getAbilities().instabuild) {
                    if (stack.is(Items.FLINT_AND_STEEL)) {
                        stack.hurtAndBreak(1, player, (living) -> living.broadcastBreakEvent(hand));
                    } else {
                        stack.shrink(1);
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    // --- Redstone: se enciende al recibir señal ---
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos,
                                Block neighbor, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, neighbor, fromPos, isMoving);
        if (!level.isClientSide && level.hasNeighborSignal(pos)) {
            prime(level, pos, state, null);
        }
    }

    // --- Reaccion en cadena: si una explosion la destruye, se enciende ---
    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        // state es el estado original (aun la TNT): la reenciende en su lugar
        prime(level, pos, state, null);
    }
}
