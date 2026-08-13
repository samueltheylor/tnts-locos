package com.tnts.config;

import com.tnts.block.TntEffect;
import com.tnts.block.TntProperties;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;

/** Valores por defecto de cada TNT (se usan al registrar y como base de la config). */
public class TntDefaults {

    public static final Map<String, TntProperties> DEFAULTS = new LinkedHashMap<>();

    static {
        add("mega_tnt",      new TntProperties(12.0f, false, true,  40, none()));
        add("mini_tnt",      new TntProperties(2.5f,  false, true,  30, none()));
        add("lava_tnt",      new TntProperties(6.0f,  true,  true,  40, of(TntEffect.LAVA)));
        add("rapida_tnt",    new TntProperties(4.0f,  false, true,  20, none()));
        add("hielo_tnt",     new TntProperties(4.0f,  false, true,  40, of(TntEffect.FREEZES, TntEffect.SNOW)));
        add("saltarina_tnt", new TntProperties(1.5f,  false, true,  30, of(TntEffect.LAUNCH)));
        add("nuclear_tnt",   new TntProperties(20.0f, true,  true,  50, of(TntEffect.NUCLEAR)));
        add("limpia_tnt",    new TntProperties(5.0f,  false, false, 40, none()));
        add("rayo_tnt",      new TntProperties(4.0f,  false, true,  40, of(TntEffect.LIGHTNING)));
        add("trampa_tnt",    new TntProperties(5.0f,  false, true,  80, of(TntEffect.TRAP)));
        add("oro_tnt",       new TntProperties(6.0f,  false, true,  40, of(TntEffect.GOLD)));
        add("obsidiana_tnt", new TntProperties(8.0f,  false, true,  40, of(TntEffect.OBSIDIAN)));
        add("crio_tnt",      new TntProperties(3.0f,  false, true,  40, of(TntEffect.CRYO)));
        add("xp_tnt",        new TntProperties(4.0f,  false, true,  40, of(TntEffect.XP)));
        add("agua_tnt",      new TntProperties(5.0f,  false, true,  40, of(TntEffect.WATER)));
        add("arena_tnt",     new TntProperties(4.0f,  false, true,  40, of(TntEffect.SAND)));
        add("diamante_tnt",  new TntProperties(6.0f,  false, true,  40, of(TntEffect.DIAMOND)));
        add("esmeralda_tnt", new TntProperties(6.0f,  false, true,  40, of(TntEffect.EMERALD)));
        add("negra_tnt",     new TntProperties(10.0f, false, true,  40, of(TntEffect.BLACKHOLE)));
        add("viento_tnt",    new TntProperties(4.0f,  false, false, 30, of(TntEffect.WIND)));  // rafaga: no rompe bloques
        add("inferno_tnt",   new TntProperties(7.0f,  true,  true,  40, of(TntEffect.INFERNO)));
        add("hongo_tnt",     new TntProperties(4.0f,  false, true,  40, of(TntEffect.FUNGI)));
        add("miel_tnt",      new TntProperties(4.0f,  false, true,  40, of(TntEffect.HONEY)));
        add("heal_tnt",      new TntProperties(1.5f,  false, false, 40, of(TntEffect.HEAL)));  // estallido suave que cura
        add("teleport_tnt",  new TntProperties(4.0f,  false, true,  40, of(TntEffect.TELEPORT)));
        add("confeti_tnt",   new TntProperties(3.0f,  false, true,  30, of(TntEffect.CONFETTI)));
        add("mina_tnt",      new TntProperties(3.0f,  false, true,  10, of(TntEffect.TRAP)));  // pita al activarse
        // === TNTs MASIVAS NUEVAS ===
        add("terremoto_tnt",  new TntProperties(18.0f, false, true,  60, of(TntEffect.EARTHQUAKE)));  // terremoto masivo
        add("meteorito_tnt",  new TntProperties(16.0f, true,  true,  55, of(TntEffect.METEOR)));    // lluvia de meteoritos
        add("tormenta_tnt",   new TntProperties(12.0f, true,  true,  50, of(TntEffect.STORM)));     // tormenta de rayos
        add("colosal_tnt",    new TntProperties(20.0f, true,  true,  70, of(TntEffect.COLOSSAL)));  // explosion en oleadas
        add("supernova_tnt",  new TntProperties(17.0f, true,  true,  65, of(TntEffect.SUPERNOVA))); // supernova + XP
        // === TNTs NUEVAS (1.9.3) ===
        add("toxica_tnt",      new TntProperties(4.0f,  false, false, 45, of(TntEffect.TOXIC)));      // nube venenosa, no rompe bloques
        add("fuegos_tnt",      new TntProperties(3.0f,  false, false, 40, of(TntEffect.FIREWORKS)));  // cohetes de colores, no rompe bloques
        add("gravitatoria_tnt",new TntProperties(8.0f,  false, true,  55, of(TntEffect.GRAVITY)));    // aplasta a todo contra el suelo
        // === TNTs NUEVAS (1.10.0) ===
        add("ender_tnt",      new TntProperties(5.0f,  false, true,  40, of(TntEffect.ENDER)));      // del End: teletransporta + endermites
        add("bubble_tnt",     new TntProperties(4.0f,  false, true,  40, of(TntEffect.BUBBLE)));     // burbuja: succiona y derrite hielo
        add("solar_tnt",      new TntProperties(6.0f,  true,  true,  40, of(TntEffect.SOLAR)));      // solar: hace dia e incendia
        // === TNTs NUEVAS (1.10.12) ===
        add("casa_tnt",       new TntProperties(2.0f,  false, false, 40, of(TntEffect.HOUSE)));     // construye una casa de madera
        add("mansion_tnt",    new TntProperties(2.0f,  false, false, 50, of(TntEffect.MANSION)));   // construye una mansion de lujo
        // === TNTs NUEVAS (1.10.21) ===
        add("luck_tnt",       new TntProperties(4.0f,  false, true,  40, of(TntEffect.LUCKY)));     // elige otra TNT al azar
        add("portal_tnt",     new TntProperties(2.0f,  false, false, 50, of(TntEffect.PORTAL)));    // construye un portal al Nether

        // === TNTs MASIVAS (v1.11.0) ===
        add("acidic_tnt",      new TntProperties(4.0f, false, false,  45, of(TntEffect.TOXIC)));  // TOXIC
        add("animal_tnt",      new TntProperties(3.0f, false, false,  40, of(TntEffect.HEAL)));  // HEAL
        add("cannon_tnt",      new TntProperties(5.0f, false, true,  30, of(TntEffect.LAUNCH)));  // LAUNCH
        add("catalyst_tnt",      new TntProperties(8.0f, true, true,  50, of(TntEffect.NUCLEAR)));  // NUCLEAR
        add("chicxulub_tnt",      new TntProperties(25.0f, true, true,  70, of(TntEffect.COLOSSAL)));  // COLOSSAL
        add("continental_tnt",      new TntProperties(15.0f, true, true,  60, of(TntEffect.EARTHQUAKE)));  // EARTHQUAKE
        add("custom_tnt",      new TntProperties(6.0f, true, true,  40, none()));  // EXPLODE
        add("deimos_tnt",      new TntProperties(12.0f, true, true,  55, of(TntEffect.METEOR)));  // METEOR
        add("dense_tnt",      new TntProperties(14.0f, true, true,  70, of(TntEffect.COLOSSAL)));  // COLOSSAL
        add("dust_bowl_tnt",      new TntProperties(8.0f, true, true,  40, none()));  // SAND
        add("end_gate_tnt",      new TntProperties(6.0f, true, true,  40, of(TntEffect.ENDER)));  // ENDER
        add("firestorm_tnt",      new TntProperties(10.0f, true, true,  50, of(TntEffect.STORM)));  // STORM
        add("flower_tnt",      new TntProperties(5.0f, false, false,  40, of(TntEffect.FUNGI)));  // FUNGI
        add("ghost_tnt",      new TntProperties(4.0f, false, false,  40, of(TntEffect.TELEPORT)));  // TELEPORT
        add("giant_tnt",      new TntProperties(18.0f, true, true,  70, of(TntEffect.COLOSSAL)));  // COLOSSAL
        add("global_tnt",      new TntProperties(30.0f, true, true,  50, of(TntEffect.NUCLEAR)));  // NUCLEAR
        add("gotthard_tnt",      new TntProperties(6.0f, true, true,  40, of(TntEffect.TUNNEL)));  // TUNNEL
        add("heat_death_tnt",      new TntProperties(22.0f, true, true,  65, of(TntEffect.SUPERNOVA)));  // SUPERNOVA
        add("heavens_gate_tnt",      new TntProperties(3.0f, false, false,  50, of(TntEffect.PORTAL)));  // PORTAL
        add("hells_gate_tnt",      new TntProperties(8.0f, true, true,  40, none()));  // INFERNO
        add("hexahedron_tnt",      new TntProperties(7.0f, true, true,  40, none()));  // EXPLODE
        add("hungry_tnt",      new TntProperties(12.0f, true, true,  40, of(TntEffect.BLACKHOLE)));  // BLACKHOLE
        add("hyperion_tnt",      new TntProperties(20.0f, true, true,  65, of(TntEffect.SUPERNOVA)));  // SUPERNOVA
        add("ice_age_tnt",      new TntProperties(14.0f, true, true,  40, of(TntEffect.FREEZES)));  // FREEZES
        add("icy_tnt",      new TntProperties(5.0f, true, true,  40, of(TntEffect.FREEZES, TntEffect.SNOW)));  // ICE
        add("knockback_tnt",      new TntProperties(4.0f, false, false,  40, of(TntEffect.WIND)));  // WIND
        add("leaping_tnt",      new TntProperties(3.0f, false, true,  30, of(TntEffect.LAUNCH)));  // LAUNCH
        add("levitating_tnt",      new TntProperties(4.0f, false, false,  55, of(TntEffect.GRAVITY)));  // GRAVITY
        add("lightning_storm_tnt",      new TntProperties(12.0f, true, true,  50, of(TntEffect.STORM)));  // STORM
        add("lucky_god_tnt",      new TntProperties(8.0f, true, true,  40, of(TntEffect.LUCKY)));  // LUCKY
        add("mankind_tnt",      new TntProperties(16.0f, true, true,  50, of(TntEffect.NUCLEAR)));  // NUCLEAR
        add("midas_tnt",      new TntProperties(6.0f, true, true,  40, of(TntEffect.GOLD)));  // GOLD
        add("mineral_tnt",      new TntProperties(6.0f, true, true,  40, of(TntEffect.DIAMOND)));  // DIAMOND
        add("mountaintop_tnt",      new TntProperties(12.0f, true, true,  60, of(TntEffect.EARTHQUAKE)));  // EARTHQUAKE
        add("particle_tnt",      new TntProperties(5.0f, true, true,  50, of(TntEffect.NUCLEAR)));  // NUCLEAR
        add("plantation_tnt",      new TntProperties(6.0f, false, false,  40, of(TntEffect.FUNGI)));  // FUNGI
        add("pompeii_tnt",      new TntProperties(12.0f, true, true,  40, none()));  // INFERNO
        add("poseidon_tnt",      new TntProperties(10.0f, false, false,  40, of(TntEffect.WATER)));  // WATER
        add("present_tnt",      new TntProperties(2.0f, true, true,  40, of(TntEffect.LUCKY)));  // LUCKY
        add("pulsar_tnt",      new TntProperties(8.0f, true, true,  50, of(TntEffect.STORM)));  // STORM
        add("reset_tnt",      new TntProperties(8.0f, true, true,  40, none()));  // EXPLODE
        add("reversed_tnt",      new TntProperties(5.0f, true, true,  40, none()));  // EXPLODE
        add("roulette_tnt",      new TntProperties(6.0f, true, true,  40, of(TntEffect.LUCKY)));  // LUCKY
        add("sinkhole_tnt",      new TntProperties(8.0f, true, true,  60, of(TntEffect.EARTHQUAKE)));  // EARTHQUAKE
        add("snowstorm_tnt",      new TntProperties(8.0f, true, true,  40, of(TntEffect.FREEZES)));  // FREEZES
        add("squaring_tnt",      new TntProperties(6.0f, true, true,  40, none()));  // EXPLODE
        add("tetrahedron_tnt",      new TntProperties(7.0f, true, true,  40, none()));  // EXPLODE
        add("revolution_tnt",      new TntProperties(10.0f, true, true,  60, of(TntEffect.EARTHQUAKE)));  // EARTHQUAKE
        add("tnt_x2000",      new TntProperties(22.0f, true, true,  70, of(TntEffect.COLOSSAL)));  // COLOSSAL
        add("toxic_clouds_tnt",      new TntProperties(10.0f, false, false,  45, of(TntEffect.TOXIC)));  // TOXIC
        add("tsar_bomba_tnt",      new TntProperties(35.0f, true, true,  50, of(TntEffect.NUCLEAR)));  // NUCLEAR
        add("unbreakable_tnt",      new TntProperties(2.0f, true, true,  40, none()));  // EXPLODE
        add("vicious_tnt",      new TntProperties(8.0f, true, true,  40, none()));  // INFERNO
        add("wither_storm_tnt",      new TntProperties(18.0f, true, true,  50, of(TntEffect.NUCLEAR)));  // NUCLEAR
        add("world_wools_tnt",      new TntProperties(6.0f, false, false,  40, of(TntEffect.CONFETTI)));  // CONFETI
        add("aether_tnt",      new TntProperties(6.0f, false, false,  50, of(TntEffect.PORTAL)));  // PORTAL
        add("asteroid_tnt",      new TntProperties(14.0f, true, true,  55, of(TntEffect.METEOR)));  // METEOR
        add("atlantis_tnt",      new TntProperties(10.0f, false, false,  40, of(TntEffect.WATER)));  // WATER
        add("city_firework_tnt",      new TntProperties(5.0f, false, false,  40, of(TntEffect.FIREWORKS)));  // FIREWORKS
        add("compressed_tnt",      new TntProperties(8.0f, true, true,  40, none()));  // EXPLODE
        add("death_ray_tnt",      new TntProperties(12.0f, true, true,  50, of(TntEffect.NUCLEAR)));  // NUCLEAR
        add("disintegrating_tnt",      new TntProperties(10.0f, true, true,  40, none()));  // EXPLODE
        add("doomsday_tnt",      new TntProperties(25.0f, true, true,  50, of(TntEffect.NUCLEAR)));  // NUCLEAR
        add("evil_tnt",      new TntProperties(10.0f, true, true,  40, none()));  // INFERNO
        add("extinction_tnt",      new TntProperties(28.0f, true, true,  50, of(TntEffect.NUCLEAR)));  // NUCLEAR
        add("fiery_hell_tnt",      new TntProperties(12.0f, true, true,  40, none()));  // INFERNO
        add("flak_tnt",      new TntProperties(8.0f, true, true,  50, of(TntEffect.STORM)));  // STORM
        add("flat_earth_tnt",      new TntProperties(16.0f, true, true,  60, of(TntEffect.EARTHQUAKE)));  // EARTHQUAKE
        add("fluorine_tnt",      new TntProperties(6.0f, false, false,  45, of(TntEffect.TOXIC)));  // TOXIC
        add("flying_tnt",      new TntProperties(5.0f, false, true,  30, of(TntEffect.LAUNCH)));  // LAUNCH
        add("grande_finale_tnt",      new TntProperties(10.0f, false, false,  40, of(TntEffect.FIREWORKS)));  // FIREWORKS
        add("heat_wave_tnt",      new TntProperties(10.0f, true, true,  40, of(TntEffect.SOLAR)));  // SOLAR
        add("helix_tnt",      new TntProperties(10.0f, true, true,  50, of(TntEffect.NUCLEAR)));  // NUCLEAR
        add("hydrogen_bomb_tnt",      new TntProperties(40.0f, true, true,  50, of(TntEffect.NUCLEAR)));  // NUCLEAR
        add("illuminati_tnt",      new TntProperties(8.0f, true, true,  40, of(TntEffect.LIGHTNING)));  // LIGHTNING
        add("jumping_tnt",      new TntProperties(4.0f, false, true,  30, of(TntEffect.LAUNCH)));  // LAUNCH
        add("kola_borehole_tnt",      new TntProperties(10.0f, true, true,  60, of(TntEffect.EARTHQUAKE)));  // EARTHQUAKE
        add("lucky_doomsday_tnt",      new TntProperties(15.0f, true, true,  40, of(TntEffect.LUCKY)));  // LUCKY
        add("meteor_storm_tnt",      new TntProperties(16.0f, true, true,  55, of(TntEffect.METEOR)));  // METEOR
        add("nether_tnt",      new TntProperties(10.0f, true, true,  40, none()));  // INFERNO
        add("phobos_tnt",      new TntProperties(10.0f, true, true,  55, of(TntEffect.METEOR)));  // METEOR
        add("solar_eruption_tnt",      new TntProperties(14.0f, true, true,  40, of(TntEffect.SOLAR)));  // SOLAR
        add("stone_cold_tnt",      new TntProperties(8.0f, true, true,  40, of(TntEffect.FREEZES)));  // FREEZES
        add("structure_tnt",      new TntProperties(3.0f, false, false,  40, of(TntEffect.HOUSE)));  // HOUSE
        add("tnt_rain_tnt",      new TntProperties(8.0f, true, true,  50, of(TntEffect.STORM)));  // STORM
        add("tnt_x10000",      new TntProperties(50.0f, true, true,  70, of(TntEffect.COLOSSAL)));  // COLOSSAL
        add("vredefort_tnt",      new TntProperties(20.0f, true, true,  60, of(TntEffect.EARTHQUAKE)));  // EARTHQUAKE
        add("wasteland_tnt",      new TntProperties(10.0f, false, false,  45, of(TntEffect.TOXIC)));  // TOXIC
        add("winter_tnt",      new TntProperties(10.0f, true, true,  40, of(TntEffect.FREEZES)));  // FREEZES
        add("dynamite",      new TntProperties(3.0f, true, true,  40, none()));  // EXPLODE
        add("accelerating_dynamite",      new TntProperties(4.0f, true, true,  40, none()));  // EXPLODE
        add("animal_dynamite",      new TntProperties(3.0f, false, false,  40, of(TntEffect.HEAL)));  // HEAL
        add("arrow_dynamite",      new TntProperties(4.0f, false, true,  30, of(TntEffect.LAUNCH)));  // LAUNCH
        add("big_dynamite",      new TntProperties(6.0f, true, true,  40, none()));  // EXPLODE
        add("bouncing_dynamite",      new TntProperties(3.0f, false, true,  30, of(TntEffect.LAUNCH)));  // LAUNCH
        add("chemical_dynamite",      new TntProperties(4.0f, false, false,  45, of(TntEffect.TOXIC)));  // TOXIC
        add("christmas_dynamite",      new TntProperties(3.0f, false, false,  40, of(TntEffect.CONFETTI)));  // CONFETI
        add("cluster_dynamite",      new TntProperties(5.0f, true, true,  40, none()));  // EXPLODE
        add("compact_dynamite",      new TntProperties(4.0f, true, true,  40, none()));  // EXPLODE
        add("cubic_dynamite",      new TntProperties(5.0f, true, true,  40, none()));  // EXPLODE
        add("digging_dynamite",      new TntProperties(4.0f, true, true,  40, of(TntEffect.TUNNEL)));  // TUNNEL
        add("dripstone_dynamite",      new TntProperties(4.0f, true, true,  40, of(TntEffect.TUNNEL)));  // TUNNEL
        add("dynamite_firework",      new TntProperties(3.0f, false, false,  40, of(TntEffect.FIREWORKS)));  // FIREWORKS
        add("dynamite_x5",      new TntProperties(4.0f, true, true,  40, none()));  // EXPLODE
        add("dynamite_x20",      new TntProperties(8.0f, true, true,  40, none()));  // EXPLODE
        add("dynamite_x100",      new TntProperties(15.0f, true, true,  70, of(TntEffect.COLOSSAL)));  // COLOSSAL
        add("dynamite_x500",      new TntProperties(25.0f, true, true,  50, of(TntEffect.NUCLEAR)));  // NUCLEAR
        add("end_dynamite",      new TntProperties(4.0f, true, true,  40, of(TntEffect.ENDER)));  // ENDER
        add("ender_dynamite",      new TntProperties(3.0f, false, false,  40, of(TntEffect.TELEPORT)));  // TELEPORT
        add("erupting_dynamite",      new TntProperties(5.0f, true, true,  40, none()));  // INFERNO
        add("farming_dynamite",      new TntProperties(3.0f, false, false,  40, of(TntEffect.FUNGI)));  // FUNGI
        add("fire_dynamite",      new TntProperties(4.0f, true, true,  40, of(TntEffect.FIRE)));  // FIRE
        add("flat_dynamite",      new TntProperties(6.0f, true, true,  60, of(TntEffect.EARTHQUAKE)));  // EARTHQUAKE
        add("floating_dynamite",      new TntProperties(3.0f, false, true,  30, of(TntEffect.LAUNCH)));  // LAUNCH
        add("floating_island_dynamite",      new TntProperties(5.0f, false, false,  40, of(TntEffect.HOUSE)));  // HOUSE
        add("freeze_dynamite",      new TntProperties(3.0f, true, true,  40, of(TntEffect.FREEZES, TntEffect.SNOW)));  // ICE
        add("gravity_dynamite",      new TntProperties(4.0f, false, false,  55, of(TntEffect.GRAVITY)));  // GRAVITY
        add("grove_dynamite",      new TntProperties(3.0f, false, false,  40, of(TntEffect.FUNGI)));  // FUNGI
        add("hellfire_dynamite",      new TntProperties(5.0f, true, true,  40, none()));  // INFERNO
        add("homing_dynamite",      new TntProperties(4.0f, false, false,  40, of(TntEffect.TELEPORT)));  // TELEPORT
        add("honey_dynamite",      new TntProperties(3.0f, false, false,  40, of(TntEffect.HONEY)));  // HONEY
        add("ice_meteor_dynamite",      new TntProperties(5.0f, true, true,  40, of(TntEffect.FREEZES, TntEffect.SNOW)));  // ICE
        add("igniter_dynamite",      new TntProperties(3.0f, true, true,  40, of(TntEffect.FIRE)));  // FIRE
        add("lava_ocean_dynamite",      new TntProperties(6.0f, true, true,  40, of(TntEffect.LAVA)));  // LAVA
        add("lightning_dynamite",      new TntProperties(4.0f, true, true,  50, of(TntEffect.STORM)));  // STORM
        add("lucky_dynamite",      new TntProperties(4.0f, true, true,  40, of(TntEffect.LUCKY)));  // LUCKY
        add("lush_dynamite",      new TntProperties(3.0f, false, false,  40, of(TntEffect.FUNGI)));  // FUNGI
        add("meteor_dynamite",      new TntProperties(5.0f, true, true,  55, of(TntEffect.METEOR)));  // METEOR
        add("miningflat_dynamite",      new TntProperties(5.0f, true, true,  40, of(TntEffect.TUNNEL)));  // TUNNEL
        add("multiplying_dynamite",      new TntProperties(4.0f, true, true,  40, none()));  // EXPLODE
        add("nether_grove_dynamite",      new TntProperties(4.0f, true, true,  40, none()));  // INFERNO
        add("nuclear_dynamite",      new TntProperties(10.0f, true, true,  50, of(TntEffect.NUCLEAR)));  // NUCLEAR
        add("nuclear_waste_dynamite",      new TntProperties(6.0f, false, false,  45, of(TntEffect.TOXIC)));  // TOXIC
        add("ocean_dynamite",      new TntProperties(4.0f, false, false,  40, of(TntEffect.WATER)));  // WATER
        add("physics_dynamite",      new TntProperties(5.0f, false, false,  55, of(TntEffect.GRAVITY)));  // GRAVITY
        add("picky_dynamite",      new TntProperties(3.0f, true, true,  40, none()));  // EXPLODE
        add("prism_dynamite",      new TntProperties(4.0f, true, true,  40, of(TntEffect.LIGHTNING)));  // LIGHTNING
        add("pulse_dynamite",      new TntProperties(4.0f, true, true,  50, of(TntEffect.STORM)));  // STORM
        add("rainbow_dynamite",      new TntProperties(3.0f, false, false,  40, of(TntEffect.CONFETTI)));  // CONFETI
        add("random_dynamite",      new TntProperties(4.0f, true, true,  40, of(TntEffect.LUCKY)));  // LUCKY
        add("reaction_dynamite",      new TntProperties(6.0f, true, true,  40, none()));  // EXPLODE
        add("ring_dynamite",      new TntProperties(5.0f, true, true,  40, none()));  // EXPLODE
        add("roulette_dynamite",      new TntProperties(5.0f, true, true,  40, of(TntEffect.LUCKY)));  // LUCKY
        add("sculk_dynamite",      new TntProperties(4.0f, true, true,  40, of(TntEffect.ENDER)));  // ENDER
        add("sensor_dynamite",      new TntProperties(4.0f, true, true,  80, of(TntEffect.TRAP)));  // TRAP
        add("shatterproof_dynamite",      new TntProperties(2.0f, true, true,  40, none()));  // EXPLODE
        add("snow_dynamite",      new TntProperties(3.0f, true, true,  40, of(TntEffect.FREEZES)));  // FREEZES
        add("sphere_dynamite",      new TntProperties(5.0f, true, true,  40, none()));  // EXPLODE
        add("spiral_dynamite",      new TntProperties(4.0f, true, true,  40, none()));  // EXPLODE
        add("timer_dynamite",      new TntProperties(4.0f, true, true,  80, of(TntEffect.TRAP)));  // TRAP
        add("tunneling_dynamite",      new TntProperties(5.0f, true, true,  40, of(TntEffect.TUNNEL)));  // TUNNEL
        add("ultralight_dynamite",      new TntProperties(2.0f, true, true,  40, none()));  // EXPLODE
        add("vaporize_dynamite",      new TntProperties(8.0f, true, true,  50, of(TntEffect.NUCLEAR)));  // NUCLEAR
        add("withering_dynamite",      new TntProperties(5.0f, false, false,  45, of(TntEffect.TOXIC)));  // TOXIC
        add("wool_dynamite",      new TntProperties(3.0f, false, false,  40, of(TntEffect.CONFETTI)));  // CONFETI
        add("xray_dynamite",      new TntProperties(4.0f, true, true,  40, of(TntEffect.LIGHTNING)));  // LIGHTNING

// Total: 156 entradas nuevas

    }

    private static EnumSet<TntEffect> none() {
        return EnumSet.noneOf(TntEffect.class);
    }

    private static EnumSet<TntEffect> of(TntEffect... effects) {
        return EnumSet.of(effects[0], java.util.Arrays.copyOfRange(effects, 1, effects.length));
    }

    private static void add(String name, TntProperties props) {
        DEFAULTS.put(name, props);
    }
}
