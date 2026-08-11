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
 * Pieza del almacen saqueado: 4 celdas cerradas alrededor de un pasillo en
 * cruz, muros de ladrillo con TNTs escondidas, cofres parcialmente vacios,
 * un spawner de zombies guardando la celda del fondo y agujeros en el techo.
 */
public class TntsWarehousePiece extends StructurePiece {

    public TntsWarehousePiece(BoundingBox box) {
        super(TntsStructures.WAREHOUSE_PIECE.get(), 0, box);
    }

    public TntsWarehousePiece(CompoundTag tag) {
        super(TntsStructures.WAREHOUSE_PIECE.get(), tag);
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

        BlockState brick = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState cracked = Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
        BlockState mossy = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
        BlockState hiddenTnt = ModBlocks.MEGA_TNT.get().defaultBlockState();
        BlockState mine = ModBlocks.MINA_TNT.get().defaultBlockState();
        BlockState crate = Blocks.OAK_PLANKS.defaultBlockState();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    int dx = x - pos.getX();
                    int dy = y - pos.getY();
                    int dz = z - pos.getZ();
                    boolean outerWall = dx == -8 || dx == 8 || dz == -8 || dz == 8;
                    // pasillo en cruz (dx==0 o dz==0) divide en 4 celdas
                    boolean corridor = dx == 0 || dz == 0;

                    if (dy == -3) {
                        // suelo: ladrillo con cajas de madera y una mina escondida
                        level.setBlock(new BlockPos(x, y, z),
                                (Math.abs(dx) + Math.abs(dz)) % 3 == 0 ? cracked : brick, 2);
                    } else if (dy == 5) {
                        // techo: agujeros por donde entro el ladron (4 huecos de 2x2)
                        boolean hole = (Math.abs(dx) == 3 && Math.abs(dz) == 3)
                                || (Math.abs(dx) == 1 && Math.abs(dz) == 5)
                                || (Math.abs(dx) == 5 && Math.abs(dz) == 1)
                                || (Math.abs(dx) == 6 && Math.abs(dz) == 6);
                        level.setBlock(new BlockPos(x, y, z), hole ? Blocks.AIR.defaultBlockState() : brick, 2);
                    } else if (outerWall) {
                        // muros exteriores con TNTs escondidas
                        if (dy >= 0 && dy <= 1 && (Math.abs(dx) + Math.abs(dy) + Math.abs(dz)) % 3 == 0) {
                            level.setBlock(new BlockPos(x, y, z), hiddenTnt, 2);
                        } else {
                            level.setBlock(new BlockPos(x, y, z),
                                    random.nextInt(6) == 0 ? mossy : brick, 2);
                        }
                    } else if (corridor && dy >= 0 && dy <= 2 && !(dx == 0 && dz == 0)) {
                        // muros internos del pasillo: dejan puertas en las esquinas
                        boolean doorway = (Math.abs(dx) == 4 && dz == 0)
                                || (dx == 0 && Math.abs(dz) == 4)
                                || (Math.abs(dx) == 7 && dz == 0)
                                || (dx == 0 && Math.abs(dz) == 7);
                        if (doorway) {
                            level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 2);
                        } else if (dy == 1 && (Math.abs(dx) + Math.abs(dz)) % 4 == 0) {
                            level.setBlock(new BlockPos(x, y, z), hiddenTnt, 2);
                        } else {
                            level.setBlock(new BlockPos(x, y, z), brick, 2);
                        }
                    } else {
                        level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        // cajas de madera (restos de la mercancia saqueada) en los rincones
        BlockState[] crates = {crate, crate, Blocks.OAK_SLAB.defaultBlockState(), crate};
        int[][] cratePos = {{-7, -2, -7}, {7, -2, 7}, {-7, -2, 7}, {7, -2, -7}};
        for (int i = 0; i < cratePos.length; i++) {
            int cx = pos.getX() + cratePos[i][0];
            int cy = pos.getY() + cratePos[i][1];
            int cz = pos.getZ() + cratePos[i][2];
            if (chunkBox.isInside(new BlockPos(cx, cy, cz))) {
                level.setBlock(new BlockPos(cx, cy, cz), crates[i], 2);
            }
        }

        // cofres: el del fondo aun tiene botin (el ladron no llego a todo)
        fillChest(level, chunkBox, new BlockPos(pos.getX() - 6, pos.getY() - 2, pos.getZ() - 6),
                new ItemStack(ModItems.MEGA_TNT.get(), 2),
                new ItemStack(ModItems.DIAMANTE_TNT.get(), 1),
                new ItemStack(Items.GUNPOWDER, 6),
                new ItemStack(ModItems.TNT_MANUAL.get(), 1));
        fillChest(level, chunkBox, new BlockPos(pos.getX() + 6, pos.getY() - 2, pos.getZ() + 6),
                new ItemStack(ModItems.MINI_TNT.get(), 4),
                new ItemStack(ModItems.RAPIDA_TNT.get(), 2),
                new ItemStack(Items.IRON_INGOT, 4));
        fillChest(level, chunkBox, new BlockPos(pos.getX() - 6, pos.getY() - 2, pos.getZ() + 6),
                new ItemStack(Items.GUNPOWDER, 3)); // casi vacio: lo saquearon

        // spawner de zombies en la celda noreste (guarda el almacen)
        BlockPos spawnerPos = new BlockPos(pos.getX() + 5, pos.getY(), pos.getZ() - 5);
        if (chunkBox.isInside(spawnerPos)) {
            level.setBlock(spawnerPos, Blocks.SPAWNER.defaultBlockState(), 2);
            if (level.getBlockEntity(spawnerPos) instanceof SpawnerBlockEntity spawner) {
                spawner.setEntityId(net.minecraft.world.entity.EntityType.ZOMBIE, random);
            }
            level.setBlock(spawnerPos.offset(0, -1, 0), Blocks.SMOOTH_STONE.defaultBlockState(), 2);
        }

        // minas cerca de los accesos del pasillo
        int[][] mines = {{-4, -2, 0}, {0, -2, 4}, {4, -2, 0}, {0, -2, -4}};
        for (int[] m : mines) {
            BlockPos mp = new BlockPos(pos.getX() + m[0], pos.getY() + m[1], pos.getZ() + m[2]);
            if (chunkBox.isInside(mp) && random.nextInt(3) != 0) {
                level.setBlock(mp, mine, 2);
            }
        }
    }

    private void fillChest(WorldGenLevel level, BoundingBox chunkBox, BlockPos bp, ItemStack... stacks) {
        if (!chunkBox.isInside(bp)) return;
        level.setBlock(bp, Blocks.CHEST.defaultBlockState(), 2);
        if (level.getBlockEntity(bp) instanceof ChestBlockEntity chest) {
            for (int i = 0; i < stacks.length; i++) {
                chest.setItem(i, stacks[i]);
            }
        }
    }
}
