package com.tnts.client;

import com.tnts.block.TntEffect;
import com.tnts.block.TntProperties;
import com.tnts.config.TntDefaults;
import com.tnts.config.TntsConfig;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.loading.FMLPaths;

/**
 * Pantalla de config simple: muestra los valores actuales de cada TNT y un
 * boton para abrir la carpeta de config (config/tnts-common.toml).
 */
public class TntsConfigScreen extends Screen {

    private final Screen parent;
    private int scroll = 0;

    public TntsConfigScreen(Screen parent) {
        super(Component.translatable("tnts.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int midX = this.width / 2;
        // Presets rapidos: ajustan TODAS las TNTs de una vez (sin reiniciar)
        this.addRenderableWidget(Button.builder(Component.translatable("tnts.preset.locura"),
                        b -> applyPreset(TntsConfig.PRESET_LOCURA))
                .bounds(midX - 160, 40, 96, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("tnts.preset.equilibrado"),
                        b -> applyPreset(TntsConfig.PRESET_EQUILIBRADO))
                .bounds(midX - 48, 40, 96, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("tnts.preset.suave"),
                        b -> applyPreset(TntsConfig.PRESET_SUAVE))
                .bounds(midX + 64, 40, 96, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("tnts.config.open"),
                        b -> Util.getPlatform().openUri(FMLPaths.CONFIGDIR.get().toUri()))
                .bounds(midX - 110, this.height - 34, 100, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"),
                        b -> this.onClose())
                .bounds(midX + 10, this.height - 34, 100, 20)
                .build());
    }

    private void applyPreset(TntsConfig.Preset preset) {
        TntsConfig.applyPreset(preset);
        // feedback: recarga la vista y muestra el multiplicador activo
        this.rebuildWidgets();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int max = Math.max(0, TntDefaults.DEFAULTS.size() * 22 - this.height + 70);
        this.scroll = (int) Math.max(0, Math.min(this.scroll - delta * 12, max));
        return true;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.translatable("tnts.config.hint"),
                this.width / 2, 22, 0xAAAAAA);

        // Muestra el multiplicador activo (preset)
        graphics.drawCenteredString(this.font,
                Component.translatable("tnts.preset.active",
                        String.format("%.1fx", TntsConfig.currentPowerMul())),
                this.width / 2, 66, 0xFFD700);

        int y = 82 - this.scroll;
        for (String name : TntDefaults.DEFAULTS.keySet()) {
            TntProperties p = TntsConfig.get(name);
            if (p == null) continue;
            graphics.drawString(this.font,
                    name + "  |  radio " + p.power() + "  |  mecha " + p.fuse() + "t"
                            + "  |  fuego " + (p.fire() ? "si" : "no")
                            + "  |  rompe " + (p.breaksBlocks() ? "si" : "no"),
                    16, y, 0xE0E0E0);
            StringBuilder fx = new StringBuilder("   efectos: ");
            for (TntEffect e : p.effects()) fx.append(e.name()).append(" ");
            graphics.drawString(this.font, fx.toString(), 16, y + 10, 0x909090);
            y += 22;
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}
