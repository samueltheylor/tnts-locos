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
 * Campo de Pruebas de TNT: una zona de pruebas al aire libre (15x7x15) con
 * muros de observacion de piedra, blancos de arena con TNT, trampas de placa
 * de presion sobre TNT enterrada, un spawner de esqueletos (los "maniquies"
 * de prueba) y un cofre con el premio del campo.
 */
public class TntsTestRangeStructure extends Structure {

    public static final Codec<TntsTestRangeStructure> CODEC =
            simpleCodec(TntsTestRangeStructure::new);

    public TntsTestRangeStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int x = chunkPos.getMiddleBlockX();
        int z = chunkPos.getMiddleBlockZ();
        int surfaceY = context.chunkGenerator().getFirstFreeHeight(x, z,
                Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        int roomY = Math.max(context.heightAccessor().getMinBuildHeight() + 4, surfaceY);
        BoundingBox box = BoundingBox.fromCorners(
                new BlockPos(x - 7, roomY - 2, z - 7),
                new BlockPos(x + 7, roomY + 4, z + 7));
        return Optional.of(new GenerationStub(new BlockPos(x, roomY, z), builder -> {
            builder.addPiece(new TntsTestRangePiece(box));
        }));
    }

    @Override
    public StructureType<?> type() {
        return TntsStructures.TEST_RANGE_TYPE.get();
    }
}
