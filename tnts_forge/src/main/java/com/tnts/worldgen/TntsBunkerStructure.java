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
 * Bunker de TNT abandonado: una sala subterranea de ladrillo de piedra con
 * TNTs escondidas en las paredes, minas en el suelo y un cofre con provisiones.
 */
public class TntsBunkerStructure extends Structure {

    public static final Codec<TntsBunkerStructure> CODEC =
            simpleCodec(TntsBunkerStructure::new);

    public TntsBunkerStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int x = chunkPos.getMiddleBlockX();
        int z = chunkPos.getMiddleBlockZ();
        int surfaceY = context.chunkGenerator().getFirstFreeHeight(x, z,
                Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        // Enterrar el bunker ~8 bloques bajo la superficie
        int roomY = Math.max(context.heightAccessor().getMinBuildHeight() + 4, surfaceY - 8);
        BoundingBox box = BoundingBox.fromCorners(
                new BlockPos(x - 5, roomY - 3, z - 5),
                new BlockPos(x + 5, roomY + 4, z + 5));
        return Optional.of(new GenerationStub(new BlockPos(x, roomY, z), builder -> {
            builder.addPiece(new TntsBunkerPiece(box));
        }));
    }

    @Override
    public StructureType<?> type() {
        return TntsStructures.BUNKER_TYPE.get();
    }
}
