package com.tnts.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

import java.util.Optional;

/**
 * Almacen de TNT saqueado: un gran deposito subterraneo (17x9x17) con
 * recintos cerrados divididos por muros, cofres parcialmente vacios (lo
 * saquearon), un spawner de zombies guardando lo que queda, TNTs escondidas
 * en los muros y agujeros en el techo por donde entro el ladron.
 */
public class TntsWarehouseStructure extends Structure {

    public static final Codec<TntsWarehouseStructure> CODEC =
            simpleCodec(TntsWarehouseStructure::new);

    public TntsWarehouseStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int x = chunkPos.getMiddleBlockX();
        int z = chunkPos.getMiddleBlockZ();
        int surfaceY = context.chunkGenerator().getFirstFreeHeight(x, z,
                Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        // Enterrar el almacen ~6 bloques bajo la superficie
        int roomY = Math.max(context.heightAccessor().getMinBuildHeight() + 6, surfaceY - 6);
        BoundingBox box = BoundingBox.fromCorners(
                new BlockPos(x - 8, roomY - 3, z - 8),
                new BlockPos(x + 8, roomY + 5, z + 8));
        return Optional.of(new GenerationStub(new BlockPos(x, roomY, z), builder -> {
            builder.addPiece(new TntsWarehousePiece(box));
        }));
    }

    @Override
    public StructureType<?> type() {
        return TntsStructures.WAREHOUSE_TYPE.get();
    }
}
