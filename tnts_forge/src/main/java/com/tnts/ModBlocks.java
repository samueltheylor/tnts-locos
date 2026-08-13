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
    public static final RegistryObject<Block> TOXICA_TNT = register("toxica_tnt", MapColor.COLOR_GREEN);
    public static final RegistryObject<Block> FUEGOS_TNT = register("fuegos_tnt", MapColor.COLOR_MAGENTA);
    public static final RegistryObject<Block> GRAVITATORIA_TNT = register("gravitatoria_tnt", MapColor.COLOR_PURPLE);
    // === NUEVAS 1.10.0 ===
    public static final RegistryObject<Block> ENDER_TNT = register("ender_tnt", MapColor.COLOR_PURPLE);
    public static final RegistryObject<Block> BUBBLE_TNT = register("bubble_tnt", MapColor.COLOR_LIGHT_BLUE);
    public static final RegistryObject<Block> SOLAR_TNT = register("solar_tnt", MapColor.COLOR_ORANGE);
    // === NUEVAS 1.10.12 ===
    public static final RegistryObject<Block> CASA_TNT = register("casa_tnt", MapColor.WOOD);
    public static final RegistryObject<Block> MANSION_TNT = register("mansion_tnt", MapColor.COLOR_BLUE);
    // === NUEVAS 1.10.21 ===
    public static final RegistryObject<Block> LUCK_TNT = register("luck_tnt", MapColor.COLOR_MAGENTA);
    public static final RegistryObject<Block> PORTAL_TNT = register("portal_tnt", MapColor.COLOR_PURPLE);
    /** Mesa de TNTs: la estacion de trabajo del aldeano experto. */
    public static final RegistryObject<Block> TNT_TABLE = BLOCKS.register("tnt_table",
            () -> new com.tnts.block.TntTableBlock(MapColor.COLOR_BROWN));
    /** Altar del Rey TNT: al usarlo convoca al boss (se encuentra en el bunker). */
    public static final RegistryObject<Block> TNT_ALTAR = BLOCKS.register("tnt_altar",
            () -> new com.tnts.block.TntAltarBlock(MapColor.COLOR_RED));

    private static RegistryObject<Block> register(String name, MapColor color) {
        Supplier<Block> supplier = () -> new TntBlock(name, TntDefaults.DEFAULTS.get(name), color);
        return BLOCKS.register(name, supplier);
    }

    // === REGISTRO DINAMICO: TNTs nuevas se registran desde TntDefaults ===
    /** Mapa dinamico: nombre -> RegistryObject para TODAS las TNTs (estaticas + dinamicas). */
    public static final java.util.Map<String, RegistryObject<Block>> DYNAMIC_BLOCKS = new java.util.LinkedHashMap<>();

    // Colores por defecto para las TNTs que no tienen color asignado
    private static final java.util.Map<String, MapColor> COLOR_MAP = new java.util.HashMap<>();
    static {
        // Colores predefinidos para las TNTs dinamicas
        putColor("acidic_tnt", MapColor.COLOR_GREEN);
        putColor("animal_tnt", MapColor.WOOD);
        putColor("cannon_tnt", MapColor.COLOR_GRAY);
        putColor("catalyst_tnt", MapColor.COLOR_RED);
        putColor("chicxulub_tnt", MapColor.COLOR_BROWN);
        putColor("continental_tnt", MapColor.COLOR_BROWN);
        putColor("custom_tnt", MapColor.COLOR_RED);
        putColor("deimos_tnt", MapColor.COLOR_BLACK);
        putColor("dense_tnt", MapColor.COLOR_GRAY);
        putColor("dust_bowl_tnt", MapColor.SAND);
        putColor("end_gate_tnt", MapColor.COLOR_PURPLE);
        putColor("firestorm_tnt", MapColor.COLOR_ORANGE);
        putColor("flower_tnt", MapColor.COLOR_GREEN);
        putColor("ghost_tnt", MapColor.SNOW);
        putColor("giant_tnt", MapColor.COLOR_RED);
        putColor("global_tnt", MapColor.COLOR_BLACK);
        putColor("gotthard_tnt", MapColor.COLOR_GRAY);
        putColor("heat_death_tnt", MapColor.COLOR_RED);
        putColor("heavens_gate_tnt", MapColor.COLOR_LIGHT_BLUE);
        putColor("hells_gate_tnt", MapColor.COLOR_RED);
        putColor("hexahedron_tnt", MapColor.COLOR_PURPLE);
        putColor("hungry_tnt", MapColor.COLOR_PURPLE);
        putColor("hyperion_tnt", MapColor.COLOR_PURPLE);
        putColor("ice_age_tnt", MapColor.COLOR_LIGHT_BLUE);
        putColor("icy_tnt", MapColor.COLOR_LIGHT_BLUE);
        putColor("knockback_tnt", MapColor.SNOW);
        putColor("leaping_tnt", MapColor.COLOR_PURPLE);
        putColor("levitating_tnt", MapColor.COLOR_PURPLE);
        putColor("lightning_storm_tnt", MapColor.COLOR_BLUE);
        putColor("lucky_god_tnt", MapColor.COLOR_MAGENTA);
        putColor("mankind_tnt", MapColor.COLOR_RED);
        putColor("midas_tnt", MapColor.GOLD);
        putColor("mineral_tnt", MapColor.COLOR_CYAN);
        putColor("mountaintop_tnt", MapColor.COLOR_BROWN);
        putColor("particle_tnt", MapColor.COLOR_PURPLE);
        putColor("plantation_tnt", MapColor.COLOR_GREEN);
        putColor("pompeii_tnt", MapColor.COLOR_BROWN);
        putColor("poseidon_tnt", MapColor.WATER);
        putColor("present_tnt", MapColor.COLOR_RED);
        putColor("pulsar_tnt", MapColor.COLOR_BLUE);
        putColor("reset_tnt", MapColor.SNOW);
        putColor("reversed_tnt", MapColor.COLOR_PURPLE);
        putColor("roulette_tnt", MapColor.COLOR_RED);
        putColor("sinkhole_tnt", MapColor.COLOR_GRAY);
        putColor("snowstorm_tnt", MapColor.SNOW);
        putColor("squaring_tnt", MapColor.COLOR_GRAY);
        putColor("tetrahedron_tnt", MapColor.COLOR_PURPLE);
        putColor("revolution_tnt", MapColor.COLOR_RED);
        putColor("tnt_x2000", MapColor.COLOR_RED);
        putColor("toxic_clouds_tnt", MapColor.COLOR_GREEN);
        putColor("tsar_bomba_tnt", MapColor.COLOR_BLACK);
        putColor("unbreakable_tnt", MapColor.COLOR_GRAY);
        putColor("vicious_tnt", MapColor.COLOR_ORANGE);
        putColor("wither_storm_tnt", MapColor.COLOR_BLACK);
        putColor("world_wools_tnt", MapColor.COLOR_MAGENTA);
        putColor("aether_tnt", MapColor.COLOR_LIGHT_BLUE);
        putColor("asteroid_tnt", MapColor.COLOR_GRAY);
        putColor("atlantis_tnt", MapColor.WATER);
        putColor("city_firework_tnt", MapColor.COLOR_MAGENTA);
        putColor("compressed_tnt", MapColor.COLOR_GRAY);
        putColor("death_ray_tnt", MapColor.COLOR_RED);
        putColor("disintegrating_tnt", MapColor.COLOR_GRAY);
        putColor("doomsday_tnt", MapColor.COLOR_RED);
        putColor("evil_tnt", MapColor.COLOR_ORANGE);
        putColor("extinction_tnt", MapColor.COLOR_BLACK);
        putColor("fiery_hell_tnt", MapColor.COLOR_ORANGE);
        putColor("flak_tnt", MapColor.COLOR_GRAY);
        putColor("flat_earth_tnt", MapColor.COLOR_BROWN);
        putColor("fluorine_tnt", MapColor.COLOR_GREEN);
        putColor("flying_tnt", MapColor.SNOW);
        putColor("grande_finale_tnt", MapColor.COLOR_MAGENTA);
        putColor("heat_wave_tnt", MapColor.COLOR_ORANGE);
        putColor("helix_tnt", MapColor.COLOR_PURPLE);
        putColor("hydrogen_bomb_tnt", MapColor.COLOR_BLACK);
        putColor("illuminati_tnt", MapColor.COLOR_BLUE);
        putColor("jumping_tnt", MapColor.COLOR_PURPLE);
        putColor("kola_borehole_tnt", MapColor.COLOR_GRAY);
        putColor("lucky_doomsday_tnt", MapColor.COLOR_MAGENTA);
        putColor("meteor_storm_tnt", MapColor.COLOR_BLACK);
        putColor("nether_tnt", MapColor.COLOR_RED);
        putColor("phobos_tnt", MapColor.COLOR_BLACK);
        putColor("solar_eruption_tnt", MapColor.COLOR_ORANGE);
        putColor("stone_cold_tnt", MapColor.COLOR_GRAY);
        putColor("structure_tnt", MapColor.WOOD);
        putColor("tnt_rain_tnt", MapColor.COLOR_BLUE);
        putColor("tnt_x10000", MapColor.COLOR_RED);
        putColor("vredefort_tnt", MapColor.COLOR_BROWN);
        putColor("wasteland_tnt", MapColor.COLOR_BROWN);
        putColor("winter_tnt", MapColor.SNOW);
        putColor("dynamite", MapColor.COLOR_RED);
        putColor("accelerating_dynamite", MapColor.COLOR_RED);
        putColor("animal_dynamite", MapColor.WOOD);
        putColor("arrow_dynamite", MapColor.COLOR_GRAY);
        putColor("big_dynamite", MapColor.COLOR_RED);
        putColor("bouncing_dynamite", MapColor.COLOR_PURPLE);
        putColor("chemical_dynamite", MapColor.COLOR_GREEN);
        putColor("christmas_dynamite", MapColor.COLOR_RED);
        putColor("cluster_dynamite", MapColor.COLOR_RED);
        putColor("compact_dynamite", MapColor.COLOR_GRAY);
        putColor("cubic_dynamite", MapColor.COLOR_PURPLE);
        putColor("digging_dynamite", MapColor.COLOR_BROWN);
        putColor("dripstone_dynamite", MapColor.COLOR_GRAY);
        putColor("dynamite_firework", MapColor.COLOR_MAGENTA);
        putColor("dynamite_x5", MapColor.COLOR_RED);
        putColor("dynamite_x20", MapColor.COLOR_RED);
        putColor("dynamite_x100", MapColor.COLOR_RED);
        putColor("dynamite_x500", MapColor.COLOR_BLACK);
        putColor("end_dynamite", MapColor.COLOR_PURPLE);
        putColor("ender_dynamite", MapColor.COLOR_PURPLE);
        putColor("erupting_dynamite", MapColor.COLOR_ORANGE);
        putColor("farming_dynamite", MapColor.COLOR_GREEN);
        putColor("fire_dynamite", MapColor.COLOR_ORANGE);
        putColor("flat_dynamite", MapColor.COLOR_GRAY);
        putColor("floating_dynamite", MapColor.COLOR_LIGHT_BLUE);
        putColor("floating_island_dynamite", MapColor.COLOR_GREEN);
        putColor("freeze_dynamite", MapColor.COLOR_LIGHT_BLUE);
        putColor("gravity_dynamite", MapColor.COLOR_PURPLE);
        putColor("grove_dynamite", MapColor.COLOR_GREEN);
        putColor("hellfire_dynamite", MapColor.COLOR_RED);
        putColor("homing_dynamite", MapColor.COLOR_PURPLE);
        putColor("honey_dynamite", MapColor.COLOR_ORANGE);
        putColor("ice_meteor_dynamite", MapColor.COLOR_LIGHT_BLUE);
        putColor("igniter_dynamite", MapColor.COLOR_RED);
        putColor("lava_ocean_dynamite", MapColor.COLOR_ORANGE);
        putColor("lightning_dynamite", MapColor.COLOR_BLUE);
        putColor("lucky_dynamite", MapColor.COLOR_MAGENTA);
        putColor("lush_dynamite", MapColor.COLOR_GREEN);
        putColor("meteor_dynamite", MapColor.COLOR_BLACK);
        putColor("miningflat_dynamite", MapColor.COLOR_GRAY);
        putColor("multiplying_dynamite", MapColor.COLOR_RED);
        putColor("nether_grove_dynamite", MapColor.COLOR_RED);
        putColor("nuclear_dynamite", MapColor.COLOR_BLACK);
        putColor("nuclear_waste_dynamite", MapColor.COLOR_GREEN);
        putColor("ocean_dynamite", MapColor.WATER);
        putColor("physics_dynamite", MapColor.COLOR_PURPLE);
        putColor("picky_dynamite", MapColor.COLOR_GRAY);
        putColor("prism_dynamite", MapColor.COLOR_BLUE);
        putColor("pulse_dynamite", MapColor.COLOR_BLUE);
        putColor("rainbow_dynamite", MapColor.COLOR_MAGENTA);
        putColor("random_dynamite", MapColor.COLOR_MAGENTA);
        putColor("reaction_dynamite", MapColor.COLOR_RED);
        putColor("ring_dynamite", MapColor.COLOR_PURPLE);
        putColor("roulette_dynamite", MapColor.COLOR_RED);
        putColor("sculk_dynamite", MapColor.COLOR_GRAY);
        putColor("sensor_dynamite", MapColor.COLOR_GRAY);
        putColor("shatterproof_dynamite", MapColor.SNOW);
        putColor("snow_dynamite", MapColor.SNOW);
        putColor("sphere_dynamite", MapColor.COLOR_PURPLE);
        putColor("spiral_dynamite", MapColor.COLOR_PURPLE);
        putColor("timer_dynamite", MapColor.COLOR_BROWN);
        putColor("tunneling_dynamite", MapColor.COLOR_GRAY);
        putColor("ultralight_dynamite", MapColor.SNOW);
        putColor("vaporize_dynamite", MapColor.COLOR_RED);
        putColor("withering_dynamite", MapColor.COLOR_BLACK);
        putColor("wool_dynamite", MapColor.COLOR_MAGENTA);
        putColor("xray_dynamite", MapColor.COLOR_BLUE);
    }

    private static void putColor(String name, MapColor color) {
        COLOR_MAP.put(name, color);
    }

    /** Nombres de los bloques ya registrados como campos estaticos (para evitar duplicados). */
    private static final java.util.Set<String> STATIC_NAMES = new java.util.HashSet<>();

    /** Registra dinamicamente todas las TNTs de TntDefaults que no tengan campo estatico. */
    static {
        // Recoger nombres de los campos estaticos ya registrados
        STATIC_NAMES.add("mega_tnt"); STATIC_NAMES.add("mini_tnt"); STATIC_NAMES.add("lava_tnt");
        STATIC_NAMES.add("rapida_tnt"); STATIC_NAMES.add("hielo_tnt"); STATIC_NAMES.add("saltarina_tnt");
        STATIC_NAMES.add("nuclear_tnt"); STATIC_NAMES.add("limpia_tnt"); STATIC_NAMES.add("rayo_tnt");
        STATIC_NAMES.add("trampa_tnt"); STATIC_NAMES.add("oro_tnt"); STATIC_NAMES.add("obsidiana_tnt");
        STATIC_NAMES.add("crio_tnt"); STATIC_NAMES.add("xp_tnt"); STATIC_NAMES.add("agua_tnt");
        STATIC_NAMES.add("arena_tnt"); STATIC_NAMES.add("diamante_tnt"); STATIC_NAMES.add("esmeralda_tnt");
        STATIC_NAMES.add("negra_tnt"); STATIC_NAMES.add("viento_tnt"); STATIC_NAMES.add("inferno_tnt");
        STATIC_NAMES.add("hongo_tnt"); STATIC_NAMES.add("miel_tnt"); STATIC_NAMES.add("heal_tnt");
        STATIC_NAMES.add("teleport_tnt"); STATIC_NAMES.add("confeti_tnt"); STATIC_NAMES.add("mina_tnt");
        STATIC_NAMES.add("terremoto_tnt"); STATIC_NAMES.add("meteorito_tnt"); STATIC_NAMES.add("tormenta_tnt");
        STATIC_NAMES.add("colosal_tnt"); STATIC_NAMES.add("supernova_tnt");
        STATIC_NAMES.add("toxica_tnt"); STATIC_NAMES.add("fuegos_tnt"); STATIC_NAMES.add("gravitatoria_tnt");
        STATIC_NAMES.add("ender_tnt"); STATIC_NAMES.add("bubble_tnt"); STATIC_NAMES.add("solar_tnt");
        STATIC_NAMES.add("casa_tnt"); STATIC_NAMES.add("mansion_tnt");
        STATIC_NAMES.add("luck_tnt"); STATIC_NAMES.add("portal_tnt");
        STATIC_NAMES.add("tnt_table"); STATIC_NAMES.add("tnt_altar");
        for (String name : TntDefaults.DEFAULTS.keySet()) {
            if (!STATIC_NAMES.contains(name) && !DYNAMIC_BLOCKS.containsKey(name)) {
                MapColor color = COLOR_MAP.getOrDefault(name, MapColor.COLOR_RED);
                DYNAMIC_BLOCKS.put(name, register(name, color));
            }
        }
    }

    /** Obtiene un bloque por nombre (funciona para todas las TNTs). */
    public static RegistryObject<Block> getByName(String name) {
        if (DYNAMIC_BLOCKS.containsKey(name)) return DYNAMIC_BLOCKS.get(name);
        return null;
    }

    public static List<Block> getAllBlocks() {
        java.util.List<Block> blocks = new java.util.ArrayList<>(List.of(
                MEGA_TNT.get(), MINI_TNT.get(), LAVA_TNT.get(), RAPIDA_TNT.get(),
                HIELO_TNT.get(), SALTARINA_TNT.get(), NUCLEAR_TNT.get(), LIMPIA_TNT.get(),
                RAYO_TNT.get(), TRAMPA_TNT.get(), ORO_TNT.get(), OBSIDIANA_TNT.get(),
                CRIO_TNT.get(), XP_TNT.get(), AGUA_TNT.get(), ARENA_TNT.get(),
                DIAMANTE_TNT.get(), ESMERALDA_TNT.get(), NEGRA_TNT.get(), VIENTO_TNT.get(),
                INFERNO_TNT.get(), HONGO_TNT.get(), MIEL_TNT.get(), HEAL_TNT.get(),
                TELEPORT_TNT.get(), CONFETI_TNT.get(), MINA_TNT.get(),
                TERREMOTO_TNT.get(), METEORITO_TNT.get(), TORMENTA_TNT.get(),
                COLOSAL_TNT.get(), SUPERNOVA_TNT.get(),
                TOXICA_TNT.get(), FUEGOS_TNT.get(), GRAVITATORIA_TNT.get(),
                ENDER_TNT.get(), BUBBLE_TNT.get(), SOLAR_TNT.get(),
                CASA_TNT.get(), MANSION_TNT.get(),
                LUCK_TNT.get(), PORTAL_TNT.get(),
                TNT_TABLE.get(), TNT_ALTAR.get()));
        // Anadir bloques dinamicos que no esten en la lista estatica
        java.util.Set<String> staticNames = new java.util.HashSet<>();
        for (Block b : blocks) {
            staticNames.add(net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(b).getPath());
        }
        for (var entry : DYNAMIC_BLOCKS.entrySet()) {
            if (!staticNames.contains(entry.getKey())) {
                blocks.add(entry.getValue().get());
            }
        }
        return blocks;
    }

    /** Nombres de todas las variantes (orden de la config). */
    public static List<String> getNames() {
        return List.copyOf(TntDefaults.DEFAULTS.keySet());
    }
}
