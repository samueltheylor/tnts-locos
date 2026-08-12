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
    public static final RegistryObject<Item> TOXICA_TNT = blockItem("toxica_tnt", ModBlocks.TOXICA_TNT);
    public static final RegistryObject<Item> FUEGOS_TNT = blockItem("fuegos_tnt", ModBlocks.FUEGOS_TNT);
    public static final RegistryObject<Item> GRAVITATORIA_TNT = blockItem("gravitatoria_tnt", ModBlocks.GRAVITATORIA_TNT);

    public static final RegistryObject<Item> ENDER_TNT = blockItem("ender_tnt", ModBlocks.ENDER_TNT);
    public static final RegistryObject<Item> BUBBLE_TNT = blockItem("bubble_tnt", ModBlocks.BUBBLE_TNT);
    public static final RegistryObject<Item> SOLAR_TNT = blockItem("solar_tnt", ModBlocks.SOLAR_TNT);

    public static final RegistryObject<Item> CASA_TNT = blockItem("casa_tnt", ModBlocks.CASA_TNT);
    public static final RegistryObject<Item> MANSION_TNT = blockItem("mansion_tnt", ModBlocks.MANSION_TNT);

    public static final RegistryObject<Item> TNT_TABLE = blockItem("tnt_table", ModBlocks.TNT_TABLE);
    public static final RegistryObject<Item> TNT_ALTAR = blockItem("tnt_altar", ModBlocks.TNT_ALTAR);

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

    /** Botas de TNT: doble salto con impulso de explosion al saltar en el aire. */
    public static final RegistryObject<Item> TNT_BOOTS = ITEMS.register("tnt_boots",
            () -> new ArmorItem(TNT_ARMOR, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(195)));

    /** Casco de TNT: vision nocturna mientras lo llevas puesto. */
    public static final RegistryObject<Item> TNT_HELMET = ITEMS.register("tnt_helmet",
            () -> new ArmorItem(TNT_ARMOR, ArmorItem.Type.HELMET,
                    new Item.Properties().durability(165)));

    /** Pantalon de TNT: completa la armadura (set bonus de 4 piezas). */
    public static final RegistryObject<Item> TNT_LEGGINGS = ITEMS.register("tnt_leggings",
            () -> new ArmorItem(TNT_ARMOR, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(225)));

    public static final RegistryObject<Item> TNT_PICKAXE = ITEMS.register("tnt_pickaxe",
            () -> new TntPickaxeItem(Tiers.DIAMOND, 1, -2.8F,
                    new Item.Properties().durability(800)));

    /** Manual de TNTs: abre un GUI con todas las TNTs del mod. */
    public static final RegistryObject<Item> TNT_MANUAL = ITEMS.register("tnt_manual",
            () -> new com.tnts.item.TntManualItem(new Item.Properties().stacksTo(1)));

    /** Disco de musica con el tema del mod (tocable en la jukebox). */
    public static final RegistryObject<Item> TNT_DISC = ITEMS.register("tnt_disc",
            () -> new net.minecraft.world.item.RecordItem(15, ModSounds.TNT_MUSIC.get(),
                    new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.RARE), 170));

    /** Corona del Rey TNT: botin del boss. Al llevarla, tus TNTs explotan con +50%% de radio. */
    public static final RegistryObject<Item> TNT_KING_CROWN = ITEMS.register("tnt_king_crown",
            () -> new com.tnts.item.TntKingCrownItem(TNT_ARMOR, net.minecraft.world.item.ArmorItem.Type.HELMET,
                    new Item.Properties().durability(330)));

    /** Huevo de invocacion del Rey TNT. */
    public static final RegistryObject<Item> TNT_KING_SPAWN_EGG = ITEMS.register("tnt_king_spawn_egg",
            () -> new net.minecraftforge.common.ForgeSpawnEggItem(
                    com.tnts.entity.TntsEntities.TNT_KING, 0x7f1d1d, 0xfde047,
                    new Item.Properties()));

    /** Espada del Rey TNT: botin raro del boss. Enciende TNTs al golpear. */
    public static final RegistryObject<Item> TNT_KING_SWORD = ITEMS.register("tnt_king_sword",
            () -> new com.tnts.item.TntKingSwordItem(
                    new Item.Properties().durability(1561).rarity(net.minecraft.world.item.Rarity.EPIC)));

    /** Escudo de TNT: botin raro del boss. Empuja a los atacantes al bloquear. */
    public static final RegistryObject<Item> TNT_SHIELD = ITEMS.register("tnt_shield",
            () -> new com.tnts.item.TntShieldItem(
                    new Item.Properties().durability(480).rarity(net.minecraft.world.item.Rarity.EPIC)));

    private static RegistryObject<Item> blockItem(String name, RegistryObject<Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}
