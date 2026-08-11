package com.tnts.client;

import net.minecraft.client.Minecraft;

/**
 * Helper SOLO cliente para abrir el Manual de TNTs. Separado en su propia
 * clase para que el item comun (TntManualItem) solo haga referencia a este
 * metodo vacio (sin tipos de cliente en la firma) y el servidor dedicado
 * nunca cargue las clases de pantalla (RuntimeDistCleaner no puede quitar
 * referencias directas a Screen desde codigo comun).
 */
public final class TntManualClient {

    private TntManualClient() {
    }

    /** Abre la pantalla del manual (solo se invoca en el cliente). */
    public static void open() {
        Minecraft.getInstance().setScreen(new TntManualScreen());
    }
}
