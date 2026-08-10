package com.tnts.entity;

import java.util.Map;

/**
 * Colores de estallido por variante: cada TNT explota con un estallido de
 * polvo de su color, para que ninguna se vea igual que otra.
 */
public final class TntVfx {

    private TntVfx() {
    }

    private static final Map<String, int[]> COLORS = Map.ofEntries(
            Map.entry("mega_tnt", new int[]{127, 29, 29}),
            Map.entry("mini_tnt", new int[]{71, 85, 105}),
            Map.entry("lava_tnt", new int[]{194, 65, 12}),
            Map.entry("rapida_tnt", new int[]{20, 83, 45}),
            Map.entry("hielo_tnt", new int[]{2, 132, 199}),
            Map.entry("saltarina_tnt", new int[]{109, 40, 217}),
            Map.entry("nuclear_tnt", new int[]{250, 204, 21}),
            Map.entry("limpia_tnt", new int[]{231, 229, 228}),
            Map.entry("rayo_tnt", new int[]{253, 224, 71}),
            Map.entry("trampa_tnt", new int[]{245, 158, 11}),
            Map.entry("oro_tnt", new int[]{253, 224, 71}),
            Map.entry("obsidiana_tnt", new int[]{139, 92, 246}),
            Map.entry("crio_tnt", new int[]{103, 232, 249}),
            Map.entry("xp_tnt", new int[]{163, 230, 53}),
            Map.entry("agua_tnt", new int[]{56, 189, 248}),
            Map.entry("arena_tnt", new int[]{202, 138, 4}),
            Map.entry("diamante_tnt", new int[]{103, 232, 249}),
            Map.entry("esmeralda_tnt", new int[]{52, 211, 153}),
            Map.entry("negra_tnt", new int[]{168, 85, 247}),
            Map.entry("viento_tnt", new int[]{226, 232, 240}),
            Map.entry("inferno_tnt", new int[]{249, 115, 22}),
            Map.entry("hongo_tnt", new int[]{252, 165, 165}),
            Map.entry("miel_tnt", new int[]{251, 191, 36}),
            Map.entry("heal_tnt", new int[]{249, 168, 212}),
            Map.entry("teleport_tnt", new int[]{192, 132, 252}),
            Map.entry("confeti_tnt", new int[]{244, 114, 182}),
            Map.entry("mina_tnt", new int[]{168, 162, 158}));

    /** Color RGB del estallido de una variante (rojo por defecto). */
    public static int[] colorOf(String variant) {
        int[] c = COLORS.get(variant);
        return c != null ? c : new int[]{255, 80, 30};
    }
}
