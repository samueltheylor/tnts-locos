package com.tnts.worldgen;

import com.tnts.TntsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registros de estructuras del mod (1.20.1: tipo de estructura y tipo de pieza
 * van en registries de vanilla, no de Forge).
 */
public class TntsStructures {

    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, TntsMod.MODID);

    public static final DeferredRegister<StructurePieceType> PIECE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, TntsMod.MODID);

    public static final RegistryObject<StructureType<TntsBunkerStructure>> BUNKER_TYPE =
            STRUCTURE_TYPES.register("tnts_bunker", () -> () -> TntsBunkerStructure.CODEC);

    public static final RegistryObject<StructurePieceType> BUNKER_PIECE =
            PIECE_TYPES.register("tnts_bunker", () -> (ctx, tag) -> new TntsBunkerPiece(tag));
}
