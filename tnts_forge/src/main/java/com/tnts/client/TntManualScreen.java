package com.tnts.client;

import com.tnts.block.TntProperties;
import com.tnts.config.TntDefaults;
import com.tnts.config.TntsConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Pantalla del Manual de TNTs: una lista con scroll de todas las TNTs del
 * mod — icono, nombre, efecto y radio de explosion. Se abre con el item
 * "Manual de TNTs" (click derecho).
 */
public class TntManualScreen extends Screen {

    private static final int ROW_HEIGHT = 24;

    private int scroll = 0;

    public TntManualScreen() {
        super(Component.translatable("item.tnts.tnt_manual"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int max = Math.max(0, TntDefaults.DEFAULTS.size() * ROW_HEIGHT - this.height + 60);
        this.scroll = (int) Math.max(0, Math.min(this.scroll - delta * 14, max));
        return true;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
                Component.translatable("tnts.manual.hint"), this.width / 2, 22, 0xAAAAAA);

        int y = 34 - this.scroll;
        for (String name : TntDefaults.DEFAULTS.keySet()) {
            if (y + ROW_HEIGHT < 30 || y > this.height - 10) {
                y += ROW_HEIGHT;
                continue;
            }
            // icono de la TNT (guard defensivo: si el item no existe,
            // new ItemStack(null) crashearia antes de poder comprobar nada)
            net.minecraft.world.item.Item item =
                    ForgeRegistries.ITEMS.getValue(new ResourceLocation("tnts", name));
            if (item != null) {
                graphics.renderItem(new ItemStack(item), 22, y + 2);
            }
            // nombre + efecto
            graphics.drawString(this.font, Component.translatable("block.tnts." + name),
                    44, y + 1, 0xFFFFFF);
            Component desc = Component.translatable("tnts.manual." + name);
            graphics.drawString(this.font, this.font.plainSubstrByWidth(desc.getString(), 200),
                    44, y + 11, 0xA0A0A0);
            // radio de explosion (config)
            TntProperties props = TntsConfig.get(name);
            if (props != null) {
                graphics.drawString(this.font,
                        Component.translatable("tnts.manual.power", String.format("%.0f", props.power())),
                        this.width - 60, y + 5, 0xFFD700);
            }
            y += ROW_HEIGHT;
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
