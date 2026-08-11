package com.tnts.config;

import com.tnts.block.TntEffect;
import com.tnts.block.TntProperties;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Configuracion del mod (config/tnts-common.toml).
 * Permite ajustar radio, mecha, fuego, romper bloques y efectos de cada TNT
 * sin recompilar. Los cambios se aplican al recargar la config.
 */
public class TntsConfig {

    private static final Map<String, Supplier<TntProperties>> SETTINGS = new HashMap<>();

    /** Toggle por TNT: false = desactivada (no se enciende con nada). */
    private static final Map<String, Supplier<Boolean>> ENABLED = new HashMap<>();

    /** Calidad global de particulas (0 = modo PC debil, 1 = normal, 2 = maxima). */
    private static ForgeConfigSpec.IntValue particleQuality;

    /**
     * Preset activo elegido desde la pantalla de config (se aplica al momento
     * sobre todos los valores). 1.0 = equilibrado (los valores del toml).
     * En singleplayer se comparte entre cliente y servidor integrado.
     */
    private static volatile double presetPower = 1.0;
    private static volatile double presetFuse = 1.0;

    /** Presets rapidos: nombre -> (multiplicador de poder, multiplicador de mecha). */
    public record Preset(String name, double powerMul, double fuseMul) {}

    public static final Preset PRESET_LOCURA = new Preset("tnts.preset.locura", 3.0, 0.7);
    public static final Preset PRESET_EQUILIBRADO = new Preset("tnts.preset.equilibrado", 1.0, 1.0);
    public static final Preset PRESET_SUAVE = new Preset("tnts.preset.suave", 0.5, 1.4);

    public static void applyPreset(Preset preset) {
        presetPower = preset.powerMul();
        presetFuse = preset.fuseMul();
    }

    public static double currentPowerMul() {
        return presetPower;
    }

    public static final ForgeConfigSpec SPEC;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        for (Map.Entry<String, TntProperties> entry : TntDefaults.DEFAULTS.entrySet()) {
            String name = entry.getKey();
            TntProperties d = entry.getValue();

            builder.push(name);
            ForgeConfigSpec.BooleanValue enabled = builder.comment("false = desactiva esta TNT (no se enciende con mechero, redstone, detonador ni en cadena)")
                    .define("enabled", true);
            ForgeConfigSpec.DoubleValue power = builder.comment("Radio de la explosion (mas grande = mas boom)")
                    .defineInRange("power", d.power(), 0.1, 100.0);
            ForgeConfigSpec.BooleanValue fire = builder.comment("Incendia los bloques cercanos")
                    .define("fire", d.fire());
            ForgeConfigSpec.BooleanValue breaksBlocks = builder.comment("true = rompe bloques, false = solo daña entidades")
                    .define("breaksBlocks", d.breaksBlocks());
            ForgeConfigSpec.IntValue fuse = builder.comment("Duracion de la mecha en ticks (20 ticks = 1 segundo)")
                    .defineInRange("fuse", d.fuse(), 1, 200);
            ForgeConfigSpec.ConfigValue<List<? extends String>> effects = builder.comment(
                            "Efectos especiales (lista). Valores: " +
                            "FREEZES, SNOW, LAVA, LAUNCH, NUCLEAR, LIGHTNING, GOLD, TRAP, " +
                            "OBSIDIAN, CRYO, XP, WATER, SAND, DIAMOND, EMERALD, BLACKHOLE, " +
                            "WIND, INFERNO, FUNGI, HONEY, HEAL, TELEPORT, CONFETTI, " +
                            "TOXIC, FIREWORKS, GRAVITY")
                    .defineList("effects",
                            d.effects().stream().map(Enum::name).toList(),
                            obj -> obj instanceof String);
            builder.pop();

            ENABLED.put(name, enabled::get);

            // Los .get() solo se llaman al acceder (despues de construir el SPEC)
            SETTINGS.put(name, () -> {
                EnumSet<TntEffect> fx = EnumSet.noneOf(TntEffect.class);
                for (String s : effects.get()) {
                    try {
                        fx.add(TntEffect.valueOf(s));
                    } catch (IllegalArgumentException ignored) {
                        // valor desconocido en la config: se ignora
                    }
                }
                return new TntProperties(
                        power.get().floatValue(), fire.get(), breaksBlocks.get(), fuse.get(), fx);
            });
        }

        // Seccion general (ajustes globales)
        builder.push("general");
        particleQuality = builder.comment("Calidad de particulas: 0 = modo PC debil (menos particulas, mejor rendimiento), "
                        + "1 = normal, 2 = maxima (mas espectaculo)")
                .defineInRange("particleQuality", 1, 0, 2);
        builder.pop();

        SPEC = builder.build();

        // Aplica el preset runtime sobre los valores del toml.
        SETTINGS.replaceAll((name, supplier) -> () -> {
            TntProperties p = supplier.get();
            float newPower = (float) (p.power() * presetPower);
            int newFuse = Math.max(1, (int) Math.round(p.fuse() * presetFuse));
            return new TntProperties(newPower, p.fire(), p.breaksBlocks(), newFuse, p.effects());
        });
    }

    /** Devuelve la config actual de un bloque (o null si no existe). */
    public static TntProperties get(String blockName) {
        Supplier<TntProperties> supplier = SETTINGS.get(blockName);
        return supplier != null ? supplier.get() : null;
    }

    /** Esta TNT activada en config? (false = no se enciende con nada). */
    public static boolean isEnabled(String blockName) {
        Supplier<Boolean> enabled = ENABLED.get(blockName);
        return enabled == null || enabled.get();
    }

    /** Multiplicador de particulas segun la calidad elegida en config. */
    public static float particleMul() {
        int q = particleQuality != null ? particleQuality.get() : 1;
        return q == 0 ? 0.35f : (q == 2 ? 1.8f : 1.0f);
    }

    /** Escala una cantidad de particulas por la calidad configurada. */
    public static int particles(int count) {
        return Math.max(1, Math.round(count * particleMul()));
    }

    /** Calidad maxima (particleQuality = 2): espectaculo extra en las masivas. */
    public static boolean maxQuality() {
        return particleQuality != null && particleQuality.get() == 2;
    }
}
