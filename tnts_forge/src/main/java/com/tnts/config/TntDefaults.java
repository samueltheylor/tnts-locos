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
