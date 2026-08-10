package com.tnts;

import com.tnts.block.TntBlock;
import com.tnts.block.TntMineBlock;
import com.tnts.config.TntDefaults;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, TntsMod.MODID);

    public static final RegistryObject<Block> MEGA_TNT = register("mega_tnt", MapColor.COLOR_RED);
    public static final RegistryObject<Block> MINI_TNT = register("mini_tnt", MapColor.COLOR_LIGHT_GRAY);
    public static final RegistryObject<Block> LAVA_TNT = register("lava_tnt", MapColor.COLOR_ORANGE);
    public static final RegistryObject<Block> RAPIDA_TNT = register("rapida_tnt", MapColor.COLOR_GREEN);
    public static final RegistryObject<Block> HIELO_TNT = register("hielo_tnt", MapColor.COLOR_LIGHT_BLUE);
    public static final RegistryObject<Block> SALTARINA_TNT = register("saltarina_tnt", MapColor.COLOR_PURPLE);
    public static final RegistryObject<Block> NUCLEAR_TNT = register("nuclear_tnt", MapColor.COLOR_BLACK);
    public static final RegistryObject<Block> LIMPIA_TNT = register("limpia_tnt", MapColor.SNOW);
    public static final RegistryObject<Block> RAYO_TNT = register("rayo_tnt", MapColor.COLOR_BLUE);
    public static final RegistryObject<Block> TRAMPA_TNT = register("trampa_tnt", MapColor.COLOR_BROWN);
    public static final RegistryObject<Block> ORO_TNT = register("oro_tnt", MapColor.GOLD);
    public static final RegistryObject<Block> OBSIDIANA_TNT = register("obsidiana_tnt", MapColor.COLOR_PURPLE);
    public static final RegistryObject<Block> CRIO_TNT = register("crio_tnt", MapColor.COLOR_CYAN);
    public static final RegistryObject<Block> XP_TNT = register("xp_tnt", MapColor.COLOR_LIGHT_GREEN);
    public static final RegistryObject<Block> AGUA_TNT = register("agua_tnt", MapColor.WATER);
    public static final RegistryObject<Block> ARENA_TNT = register("arena_tnt", MapColor.SAND);
    public static final RegistryObject<Block> DIAMANTE_TNT = register("diamante_tnt", MapColor.COLOR_CYAN);
    public static final RegistryObject<Block> ESMERALDA_TNT = register("esmeralda_tnt", MapColor.COLOR_GREEN);
    public static final RegistryObject<Block> NEGRA_TNT = register("negra_tnt", MapColor.COLOR_PURPLE);
    public static final RegistryObject<Block> VIENTO_TNT = register("viento_tnt", MapColor.SNOW);
    public static final RegistryObject<Block> INFERNO_TNT = register("inferno_tnt", MapColor.COLOR_ORANGE);
    public static final RegistryObject<Block> HONGO_TNT = register("hongo_tnt", MapColor.COLOR_RED);
    public static final RegistryObject<Block> MIEL_TNT = register("miel_tnt", MapColor.COLOR_ORANGE);
    public static final RegistryObject<Block> HEAL_TNT = register("heal_tnt", MapColor.COLOR_PINK);
    public static final RegistryObject<Block> TELEPORT_TNT = register("teleport_tnt", MapColor.COLOR_PURPLE);
    public static final RegistryObject<Block> CONFETI_TNT = register("confeti_tnt", MapColor.COLOR_MAGENTA);
    public static final RegistryObject<Block> MINA_TNT = BLOCKS.register("mina_tnt",
            () -> new TntMineBlock("mina_tnt", TntDefaults.DEFAULTS.get("mina_tnt"), MapColor.COLOR_GRAY));
    public static final RegistryObject<Block> TERREMOTO_TNT = register("terremoto_tnt", MapColor.COLOR_BROWN);
    public static final RegistryObject<Block> METEORITO_TNT = register("meteorito_tnt", MapColor.COLOR_BLACK);
    public static final RegistryObject<Block> TORMENTA_TNT = register("tormenta_tnt", MapColor.COLOR_BLUE);
    public static final RegistryObject<Block> COLOSAL_TNT = register("colosal_tnt", MapColor.COLOR_RED);
    public static final RegistryObject<Block> SUPERNOVA_TNT = register("supernova_tnt", MapColor.COLOR_PURPLE);

    private static RegistryObject<Block> register(String name, MapColor color) {
        Supplier<Block> supplier = () -> new TntBlock(name, TntDefaults.DEFAULTS.get(name), color);
        return BLOCKS.register(name, supplier);
    }

    public static List<Block> getAllBlocks() {
        return List.of(
                MEGA_TNT.get(), MINI_TNT.get(), LAVA_TNT.get(), RAPIDA_TNT.get(),
                HIELO_TNT.get(), SALTARINA_TNT.get(), NUCLEAR_TNT.get(), LIMPIA_TNT.get(),
                RAYO_TNT.get(), TRAMPA_TNT.get(), ORO_TNT.get(), OBSIDIANA_TNT.get(),
                CRIO_TNT.get(), XP_TNT.get(), AGUA_TNT.get(), ARENA_TNT.get(),
                DIAMANTE_TNT.get(), ESMERALDA_TNT.get(), NEGRA_TNT.get(), VIENTO_TNT.get(),
                INFERNO_TNT.get(), HONGO_TNT.get(), MIEL_TNT.get(), HEAL_TNT.get(),
                TELEPORT_TNT.get(), CONFETI_TNT.get(), MINA_TNT.get(),
                TERREMOTO_TNT.get(), METEORITO_TNT.get(), TORMENTA_TNT.get(),
                COLOSAL_TNT.get(), SUPERNOVA_TNT.get());
    }

    /** Nombres de todas las variantes (orden de la config). */
    public static List<String> getNames() {
        return List.copyOf(TntDefaults.DEFAULTS.keySet());
    }
}
