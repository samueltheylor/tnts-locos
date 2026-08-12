package com.tnts.worldgen;

import com.tnts.ModBlocks;
import com.tnts.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

/**
 * Pieza del bunker: una sala de 11x8x11 con paredes de ladrillo de piedra,
 * TNTs escondidas en las paredes, minas en el suelo, antorchas y un cofre.
 */
public class TntsBunkerPiece extends StructurePiece {

    public TntsBunkerPiece(BoundingBox box) {
        super(TntsStructures.BUNKER_PIECE.get(), 0, box);
    }

    public TntsBunkerPiece(CompoundTag tag) {
        super(TntsStructures.BUNKER_PIECE.get(), tag);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        // sin datos extra: el bounding box ya se guarda solo
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                            RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pos) {
        int minX = Math.max(this.boundingBox.minX(), chunkBox.minX());
        int maxX = Math.min(this.boundingBox.maxX(), chunkBox.maxX());
        int minY = Math.max(this.boundingBox.minY(), chunkBox.minY());
        int maxY = Math.min(this.boundingBox.maxY(), chunkBox.maxY());
        int minZ = Math.max(this.boundingBox.minZ(), chunkBox.minZ());
        int maxZ = Math.min(this.boundingBox.maxZ(), chunkBox.maxZ());

        BlockState stoneBricks = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState cracked = Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
        BlockState hiddenTnt = ModBlocks.MEGA_TNT.get().defaultBlockState();
        BlockState mine = ModBlocks.MINA_TNT.get().defaultBlockState();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    int dx = x - pos.getX();
                    int dy = y - pos.getY();
                    int dz = z - pos.getZ();
                    boolean wall = dx == -5 || dx == 5 || dz == -5 || dz == 5;
                    if (dy == -3) {
                        // suelo
                        level.setBlock(new BlockPos(x, y, z),
                                (dx + dz) % 4 == 0 ? cracked : stoneBricks, 2);
                    } else if (dy == 4) {
                        // techo
                        level.setBlock(new BlockPos(x, y, z), stoneBricks, 2);
                    } else if (wall) {
                        // paredes con TNT escondidas cada 3 bloques
                        if (dy >= -1 && dy <= 1 && (Math.abs(dx) + Math.abs(dy) + Math.abs(dz)) % 3 == 0) {
                            level.setBlock(new BlockPos(x, y, z), hiddenTnt, 2);
                        } else {
                            level.setBlock(new BlockPos(x, y, z), stoneBricks, 2);
                        }
                    } else if (dy == -2 && ((dx == -3 && dz == -3) || (dx == 0 && dz == 3))) {
                        // minas en el suelo
                        level.setBlock(new BlockPos(x, y, z), mine, 2);
                    } else if (dx == 0 && dy == -2 && dz == 0) {
                        // ALTAR DEL REY TNT en el centro: al usarlo convoca al jefe
                        level.setBlock(new BlockPos(x, y, z),
                                ModBlocks.TNT_ALTAR.get().defaultBlockState(), 2);
                    } else if (dx == 3 && dy == -2 && dz == 3) {
                        // cofre con provisiones en una esquina
                        level.setBlock(new BlockPos(x, y, z), Blocks.CHEST.defaultBlockState(), 2);
                        if (level.getBlockEntity(new BlockPos(x, y, z)) instanceof ChestBlockEntity chest) {
                            chest.setItem(0, new ItemStack(ModItems.MINI_TNT.get(), 3));
                            chest.setItem(1, new ItemStack(ModItems.MEGA_TNT.get(), 1));
                            chest.setItem(2, new ItemStack(Items.GUNPOWDER, 4));
                            chest.setItem(3, new ItemStack(Items.IRON_INGOT, 3));
                            chest.setItem(4, new ItemStack(ModItems.TNT_MANUAL.get(), 1));
                        }
                    } else {
                        level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        // EL REY TNT: vive en el bunker. Aparece en el centro (sobre el altar)
        // con su barra de jefe completa. 60% de probabilidades.
        if (random.nextFloat() < 0.6F && chunkBox.isInside(new BlockPos(pos.getX(), pos.getY() - 2, pos.getZ()))) {
            BlockPos kingPos = new BlockPos(pos.getX(), pos.getY() - 2, pos.getZ());
            com.tnts.entity.TntKingEntity king = new com.tnts.entity.TntKingEntity(
                    com.tnts.entity.TntsEntities.TNT_KING.get(), level.getLevel());
            king.moveTo(kingPos.getX() + 0.5, kingPos.getY() + 1, kingPos.getZ() + 0.5,
                    level.getRandom().nextFloat() * 360.0F, 0.0F);
            level.getLevel().addFreshEntity(king);
        }

        // antorchas en las paredes mirando hacia dentro
        if (chunkBox.isInside(new BlockPos(pos.getX() - 5, pos.getY() + 1, pos.getZ()))) {
            level.setBlock(new BlockPos(pos.getX() - 5, pos.getY() + 1, pos.getZ()),
                    Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST), 2);
        }
        if (chunkBox.isInside(new BlockPos(pos.getX() + 5, pos.getY() + 1, pos.getZ()))) {
            level.setBlock(new BlockPos(pos.getX() + 5, pos.getY() + 1, pos.getZ()),
                    Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.WEST), 2);
        }
        if (chunkBox.isInside(new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ() - 5))) {
            level.setBlock(new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ() - 5),
                    Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.SOUTH), 2);
        }
        if (chunkBox.isInside(new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ() + 5))) {
            level.setBlock(new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ() + 5),
                    Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.NORTH), 2);
        }
    }
}
