package com.tnts.worldgen;

import com.tnts.ModBlocks;
import com.tnts.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

/**
 * Pieza del campo de pruebas: pista de arena delimitada por muros de
 * observacion (piedra con ventanas), 4 blancos de piedra con una TNT encima,
 * trampas de placa de presion sobre TNT enterrada, un spawner de esqueletos
 * en la plataforma central y un cofre con el premio del campo.
 */
public class TntsTestRangePiece extends StructurePiece {

    public TntsTestRangePiece(BoundingBox box) {
        super(TntsStructures.TEST_RANGE_PIECE.get(), 0, box);
    }

    public TntsTestRangePiece(CompoundTag tag) {
        super(TntsStructures.TEST_RANGE_PIECE.get(), tag);
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

        BlockState stone = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState cracked = Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
        BlockState sand = Blocks.SAND.defaultBlockState();
        BlockState tnt = ModBlocks.MEGA_TNT.get().defaultBlockState();
        BlockState hiddenTnt = ModBlocks.NUCLEAR_TNT.get().defaultBlockState();
        BlockState pressure = Blocks.STONE_PRESSURE_PLATE.defaultBlockState();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    int dx = x - pos.getX();
                    int dy = y - pos.getY();
                    int dz = z - pos.getZ();
                    boolean wall = Math.abs(dx) == 7 || Math.abs(dz) == 7;

                    if (dy == -2) {
                        // base solida debajo del suelo
                        level.setBlock(new BlockPos(x, y, z), Blocks.STONE.defaultBlockState(), 2);
                    } else if (dy == -1) {
                        // suelo de arena del campo (con trampas debajo)
                        boolean trap = (Math.abs(dx) == 3 && Math.abs(dz) == 3)
                                || (Math.abs(dx) == 3 && Math.abs(dz) == 5)
                                || (Math.abs(dx) == 5 && Math.abs(dz) == 3);
                        level.setBlock(new BlockPos(x, y, z), sand, 2);
                        if (trap) {
                            level.setBlock(new BlockPos(x, y - 1, z), hiddenTnt, 2);
                        }
                    } else if (wall) {
                        // muros de observacion con ventanas y antorchas
                        boolean window = (Math.abs(dx) == 7 && dy == 1 && Math.abs(dz) % 4 == 0)
                                || (Math.abs(dz) == 7 && dy == 1 && Math.abs(dx) % 4 == 0);
                        level.setBlock(new BlockPos(x, y, z),
                                window ? Blocks.AIR.defaultBlockState()
                                        : (random.nextInt(5) == 0 ? cracked : stone), 2);
                    } else if (dy == 0) {
                        // nivel del suelo: arena, trampas con placa encima y paso central
                        boolean trapPlate = (Math.abs(dx) == 3 && Math.abs(dz) == 3)
                                || (Math.abs(dx) == 3 && Math.abs(dz) == 5)
                                || (Math.abs(dx) == 5 && Math.abs(dz) == 3);
                        level.setBlock(new BlockPos(x, y, z),
                                trapPlate ? pressure : Blocks.AIR.defaultBlockState(), 2);
                    } else {
                        level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        // 4 blancos de prueba: columna de piedra con una TNT encima
        int[][] targets = {{-5, 0, -5}, {5, 0, -5}, {-5, 0, 5}, {5, 0, 5}};
        for (int[] t : targets) {
            BlockPos base = new BlockPos(pos.getX() + t[0], pos.getY() + t[1], pos.getZ() + t[2]);
            for (int i = 0; i <= 2; i++) {
                BlockPos p = base.offset(0, i, 0);
                if (chunkBox.isInside(p)) {
                    level.setBlock(p, Blocks.SMOOTH_STONE.defaultBlockState(), 2);
                }
            }
            BlockPos top = base.offset(0, 3, 0);
            if (chunkBox.isInside(top)) {
                level.setBlock(top, tnt, 2);
            }
        }

        // plataforma central con el spawner de esqueletos (los maniquies vivos)
        BlockPos platform = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
        for (int i = 0; i <= 1; i++) {
            BlockPos p = platform.offset(0, i, 0);
            if (chunkBox.isInside(p)) {
                level.setBlock(p, Blocks.SMOOTH_STONE.defaultBlockState(), 2);
            }
        }
        BlockPos spawnerPos = platform.offset(0, 2, 0);
        if (chunkBox.isInside(spawnerPos)) {
            level.setBlock(spawnerPos, Blocks.SPAWNER.defaultBlockState(), 2);
            if (level.getBlockEntity(spawnerPos) instanceof SpawnerBlockEntity spawner) {
                spawner.setEntityId(net.minecraft.world.entity.EntityType.SKELETON, random);
            }
        }

        // cofre con el premio del campo de pruebas
        BlockPos chestPos = new BlockPos(pos.getX() - 6, pos.getY(), pos.getZ() - 6);
        if (chunkBox.isInside(chestPos)) {
            level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
            if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
                chest.setItem(0, new ItemStack(ModItems.DETONATOR.get(), 1));
                chest.setItem(1, new ItemStack(ModItems.GRENADE.get(), 3));
                chest.setItem(2, new ItemStack(ModItems.MINI_TNT.get(), 5));
                chest.setItem(3, new ItemStack(Items.GUNPOWDER, 8));
                chest.setItem(4, new ItemStack(ModItems.TNT_MANUAL.get(), 1));
            }
        }
    }
}
