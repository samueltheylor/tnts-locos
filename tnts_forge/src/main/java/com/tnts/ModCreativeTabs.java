package com.tnts;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TntsMod.MODID);

    public static final RegistryObject<CreativeModeTab> TNT_TAB = TABS.register("tnts", () ->
            CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModBlocks.MEGA_TNT.get()))
                    .title(Component.translatable("itemGroup.tnts"))
                    .displayItems((parameters, output) -> {
                        for (Block block : ModBlocks.getAllBlocks()) {
                            output.accept(new ItemStack(block));
                        }
                        output.accept(new ItemStack(ModItems.DETONATOR.get()));
                        output.accept(new ItemStack(ModItems.LAUNCHER.get()));
                        output.accept(new ItemStack(ModItems.TNT_ARROW.get()));
                        output.accept(new ItemStack(ModItems.GRENADE.get()));
                        output.accept(new ItemStack(ModItems.TNT_CHESTPLATE.get()));
                        output.accept(new ItemStack(ModItems.TNT_BOOTS.get()));
                        output.accept(new ItemStack(ModItems.TNT_HELMET.get()));
                        output.accept(new ItemStack(ModItems.TNT_LEGGINGS.get()));
                        output.accept(new ItemStack(ModItems.TNT_PICKAXE.get()));
                        output.accept(new ItemStack(ModItems.TNT_MANUAL.get()));
                        output.accept(new ItemStack(ModItems.TNT_DISC.get()));
                        output.accept(new ItemStack(ModItems.TNT_KING_CROWN.get()));
                        output.accept(new ItemStack(ModItems.TNT_KING_SWORD.get()));
                        output.accept(new ItemStack(ModItems.TNT_SHIELD.get()));
                        output.accept(new ItemStack(ModItems.TNT_KING_SPAWN_EGG.get()));
                        output.accept(new ItemStack(ModItems.TNT_GOLEM_SPAWN_EGG.get()));
                        output.accept(new ItemStack(ModItems.TNT_ROCKET.get()));
                    })
                    .build());
}
