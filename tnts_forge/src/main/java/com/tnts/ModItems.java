package com.tnts;

import com.tnts.item.DetonatorItem;
import com.tnts.item.TntArmorMaterial;
import com.tnts.item.TntArrowItem;
import com.tnts.item.TntGrenadeItem;
import com.tnts.item.TntLauncherItem;
import com.tnts.item.TntPickaxeItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, TntsMod.MODID);

    public static final RegistryObject<Item> MEGA_TNT = blockItem("mega_tnt", ModBlocks.MEGA_TNT);
    public static final RegistryObject<Item> MINI_TNT = blockItem("mini_tnt", ModBlocks.MINI_TNT);
    public static final RegistryObject<Item> LAVA_TNT = blockItem("lava_tnt", ModBlocks.LAVA_TNT);
    public static final RegistryObject<Item> RAPIDA_TNT = blockItem("rapida_tnt", ModBlocks.RAPIDA_TNT);
    public static final RegistryObject<Item> HIELO_TNT = blockItem("hielo_tnt", ModBlocks.HIELO_TNT);
    public static final RegistryObject<Item> SALTARINA_TNT = blockItem("saltarina_tnt", ModBlocks.SALTARINA_TNT);
    public static final RegistryObject<Item> NUCLEAR_TNT = blockItem("nuclear_tnt", ModBlocks.NUCLEAR_TNT);
    public static final RegistryObject<Item> LIMPIA_TNT = blockItem("limpia_tnt", ModBlocks.LIMPIA_TNT);
    public static final RegistryObject<Item> RAYO_TNT = blockItem("rayo_tnt", ModBlocks.RAYO_TNT);
    public static final RegistryObject<Item> TRAMPA_TNT = blockItem("trampa_tnt", ModBlocks.TRAMPA_TNT);
    public static final RegistryObject<Item> ORO_TNT = blockItem("oro_tnt", ModBlocks.ORO_TNT);
    public static final RegistryObject<Item> OBSIDIANA_TNT = blockItem("obsidiana_tnt", ModBlocks.OBSIDIANA_TNT);
    public static final RegistryObject<Item> CRIO_TNT = blockItem("crio_tnt", ModBlocks.CRIO_TNT);
    public static final RegistryObject<Item> XP_TNT = blockItem("xp_tnt", ModBlocks.XP_TNT);
    public static final RegistryObject<Item> AGUA_TNT = blockItem("agua_tnt", ModBlocks.AGUA_TNT);
    public static final RegistryObject<Item> ARENA_TNT = blockItem("arena_tnt", ModBlocks.ARENA_TNT);
    public static final RegistryObject<Item> DIAMANTE_TNT = blockItem("diamante_tnt", ModBlocks.DIAMANTE_TNT);
    public static final RegistryObject<Item> ESMERALDA_TNT = blockItem("esmeralda_tnt", ModBlocks.ESMERALDA_TNT);
    public static final RegistryObject<Item> NEGRA_TNT = blockItem("negra_tnt", ModBlocks.NEGRA_TNT);
    public static final RegistryObject<Item> VIENTO_TNT = blockItem("viento_tnt", ModBlocks.VIENTO_TNT);
    public static final RegistryObject<Item> INFERNO_TNT = blockItem("inferno_tnt", ModBlocks.INFERNO_TNT);
    public static final RegistryObject<Item> HONGO_TNT = blockItem("hongo_tnt", ModBlocks.HONGO_TNT);
    public static final RegistryObject<Item> MIEL_TNT = blockItem("miel_tnt", ModBlocks.MIEL_TNT);
    public static final RegistryObject<Item> HEAL_TNT = blockItem("heal_tnt", ModBlocks.HEAL_TNT);
    public static final RegistryObject<Item> TELEPORT_TNT = blockItem("teleport_tnt", ModBlocks.TELEPORT_TNT);
    public static final RegistryObject<Item> CONFETI_TNT = blockItem("confeti_tnt", ModBlocks.CONFETI_TNT);
    public static final RegistryObject<Item> MINA_TNT = blockItem("mina_tnt", ModBlocks.MINA_TNT);
    public static final RegistryObject<Item> TERREMOTO_TNT = blockItem("terremoto_tnt", ModBlocks.TERREMOTO_TNT);
    public static final RegistryObject<Item> METEORITO_TNT = blockItem("meteorito_tnt", ModBlocks.METEORITO_TNT);
    public static final RegistryObject<Item> TORMENTA_TNT = blockItem("tormenta_tnt", ModBlocks.TORMENTA_TNT);
    public static final RegistryObject<Item> COLOSAL_TNT = blockItem("colosal_tnt", ModBlocks.COLOSAL_TNT);
    public static final RegistryObject<Item> SUPERNOVA_TNT = blockItem("supernova_tnt", ModBlocks.SUPERNOVA_TNT);

    public static final RegistryObject<Item> DETONATOR = ITEMS.register("detonator",
            () -> new DetonatorItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> LAUNCHER = ITEMS.register("launcher",
            () -> new TntLauncherItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> TNT_ARROW = ITEMS.register("tnt_arrow",
            () -> new TntArrowItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> GRENADE = ITEMS.register("grenade",
            () -> new TntGrenadeItem(new Item.Properties().stacksTo(16)));

    public static final ArmorMaterial TNT_ARMOR = new TntArmorMaterial();

    public static final RegistryObject<Item> TNT_CHESTPLATE = ITEMS.register("tnt_chestplate",
            () -> new ArmorItem(TNT_ARMOR, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(240)));

    public static final RegistryObject<Item> TNT_PICKAXE = ITEMS.register("tnt_pickaxe",
            () -> new TntPickaxeItem(Tiers.DIAMOND, 1, -2.8F,
                    new Item.Properties().durability(800)));

    private static RegistryObject<Item> blockItem(String name, RegistryObject<Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}
