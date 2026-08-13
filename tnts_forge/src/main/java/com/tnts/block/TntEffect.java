package com.tnts.block;

/** Efectos especiales que puede tener una TNT. */
public enum TntEffect {
    /** Congela el agua cercana (TNT de Hielo). */
    FREEZES,
    /** Cubre la zona con nieve (TNT de Hielo). */
    SNOW,
    /** Deja charcos de lava en el crater (TNT de Lava). */
    LAVA,
    /** Lanza entidades por los aires (TNT Saltarina). */
    LAUNCH,
    /** Espectaculo de particulas (TNT Nuclear). */
    NUCLEAR,
    /** Invoca rayos (TNT de Rayo). */
    LIGHTNING,
    /** Suelta lingotes de oro (TNT de Oro). */
    GOLD,
    /** Pitidos de cuenta atras (TNT Trampa). */
    TRAP,
    /** Convierte el crater en obsidiana (TNT de Obsidiana). */
    OBSIDIAN,
    /** Congela a los mobs y jugadores (TNT Criogenica). */
    CRYO,
    /** Suelta orbes de experiencia (TNT de Experiencia). */
    XP,
    /** Inunda el crater con agua y apaga fuego (TNT de Agua). */
    WATER,
    /** Avalancha de arena que cae sobre la zona (TNT de Arena). */
    SAND,
    /** Suelta diamantes (TNT de Diamante). */
    DIAMOND,
    /** Suelta esmeraldas (TNT de Esmeralda). */
    EMERALD,
    /** Succiona a las entidades hacia el crater (TNT Agujero Negro). */
    BLACKHOLE,
    /** Empuja a las entidades lejos con rafagas de viento (TNT de Viento). */
    WIND,
    /** Incendia un area enorme (TNT Inferno). */
    INFERNO,
    /** Esparce setas y micelio (TNT de Setas). */
    FUNGI,
    /** Lentitud pegajosa + bloques de miel (TNT de Miel). */
    HONEY,
    /** Cura y da regeneracion a los seres vivos (TNT Curativa). */
    HEAL,
    /** Teletransporta a los seres vivos a sitios aleatorios (TNT Teletransportadora). */
    TELEPORT,
    /** Lluvia de particulas de colores (TNT de Confeti). */
    CONFETTI,
    /** Terremoto: destruye bloques en ondas, lanza entidades alto, grietas (TNT Terremoto). */
    EARTHQUAKE,
    /** Meteorito: llama del cielo, meteoros de obsidiana, crater de fuego (TNT Meteorito). */
    METEOR,
    /** Tormenta: lluvia de rayos masiva, viento fuerte, cielo oscuro (TNT Tormenta). */
    STORM,
    /** Colosal: explosion en oleadas progresivas sin lag (TNT Colosal). */
    COLOSSAL,
    /** Supernova: expansion de luz seguida de colapso, drops masivos de XP (TNT Supernova). */
    SUPERNOVA,
    /** Toxica: nube verde que envenena y debilita a los seres vivos (TNT Toxica). */
    TOXIC,
    /** Fuegos Artificiales: lanza cohetes de colores al cielo (TNT de Fuegos Artificiales). */
    FIREWORKS,
    /** Gravitatoria: gravedad brutal que aplasta a todo contra el suelo (TNT Gravitatoria). */
    GRAVITY,
    /** End: teletransporta a los seres vivos a sitios aleatorios e invoca endermites (TNT del End). */
    ENDER,
    /** Burbuja: succiona a los seres e items hacia el crater y derrite el hielo (TNT Burbuja). */
    BUBBLE,
    /** Solar: hace dia, despeja el tiempo e incendia un area enorme (TNT Solar). */
    SOLAR,
    /** Casa: genera una casa acogedora de madera en el punto de explosion (TNT Casa). */
    HOUSE,
    /** Mansion: genera una mansion de lujo de dos plantas con torres y decoracion (TNT Mansion). */
    MANSION,
    /** Suerte: elige OTRA TNT del mod al azar y hace SU efecto completo (TNT de la Suerte). */
    LUCKY,
    /** Portal: construye un portal al Nether completo y encendido (TNT Portal). */
    PORTAL,
    /** Fuego: incendia el area circundante con bloques de fuego (TNTs de Fuego). */
    FIRE,
    /** Tunel: excava un tunel horizontal en la direccion mas cercana (TNTs Tuneladoras). */
    TUNNEL
}
