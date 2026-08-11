package com.tnts;

import com.google.common.collect.ImmutableSet;
import com.tnts.block.TntTableBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

/**
 * Aldeano "Experto en TNTs": una profesion nueva con su estacion de trabajo
 * (la Mesa de TNTs, un POI). Un aldeano desempleado que encuentre la mesa
 * se convierte en vendedor de TNTs: vende TNTs por esmeraldas y compra
 * polvora.
 */
public class ModVillagers {

    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, TntsMod.MODID);

    public static final DeferredRegister<VillagerProfession> PROFESSIONS =
            DeferredRegister.create(Registries.VILLAGER_PROFESSION, TntsMod.MODID);

    public static final RegistryObject<PoiType> TNT_TABLE_POI = POI_TYPES.register(
            "tnt_table", () -> new PoiType(
                    Set.of(ModBlocks.TNT_TABLE.get().defaultBlockState()), 1, 1));

    public static final RegistryObject<VillagerProfession> TNT_EXPERT = PROFESSIONS.register(
            "tnt_expert", () -> new VillagerProfession(
                    "tnt_expert",
                    holder -> holder.is(TNT_TABLE_POI.getKey()),
                    holder -> holder.is(TNT_TABLE_POI.getKey()),
                    ImmutableSet.of(ModItems.MINI_TNT.get(), ModItems.MEGA_TNT.get()),
                    ImmutableSet.of(ModBlocks.TNT_TABLE.get()),
                    ModSounds.DETONATOR_CLICK.get()));

    /** Trades del aldeano (se registran en VillagerTradesEvent). */
    public static void registerTrades(VillagerTradesEvent event) {
        if (event.getType() != TNT_EXPERT.get()) return;

        event.getTrades().get(1).add(new TradeListing(new ItemStack(Items.EMERALD, 4),
                new ItemStack(ModItems.MINI_TNT.get(), 2), 12, 6));
        event.getTrades().get(1).add(new TradeListing(new ItemStack(Items.GUNPOWDER, 6),
                new ItemStack(Items.EMERALD), 16, 4));

        event.getTrades().get(2).add(new TradeListing(new ItemStack(Items.EMERALD, 8),
                new ItemStack(ModItems.MEGA_TNT.get()), 8, 12));
        event.getTrades().get(2).add(new TradeListing(new ItemStack(Items.EMERALD, 12),
                new ItemStack(ModItems.RAPIDA_TNT.get()), 8, 12));

        event.getTrades().get(3).add(new TradeListing(new ItemStack(Items.EMERALD, 16),
                new ItemStack(ModItems.RAYO_TNT.get()), 6, 18));
        event.getTrades().get(3).add(new TradeListing(new ItemStack(Items.EMERALD, 18),
                new ItemStack(ModItems.LAVA_TNT.get()), 6, 18));

        event.getTrades().get(4).add(new TradeListing(new ItemStack(Items.EMERALD, 28),
                new ItemStack(ModItems.ORO_TNT.get()), 4, 24));
        event.getTrades().get(4).add(new TradeListing2(new ItemStack(Items.EMERALD, 22),
                new ItemStack(Items.DIAMOND), new ItemStack(ModItems.DIAMANTE_TNT.get()), 4, 28));

        event.getTrades().get(5).add(new TradeListing(new ItemStack(Items.EMERALD, 42),
                new ItemStack(ModItems.NUCLEAR_TNT.get()), 2, 40));
        event.getTrades().get(5).add(new TradeListing(new ItemStack(Items.EMERALD, 48),
                new ItemStack(ModItems.COLOSAL_TNT.get()), 2, 45));
    }

    /**
     * Listing simple: un coste (esmeraldas u otra cosa) -> un resultado.
     * Sirve tambien para compras del aldeano (coste = lo que le vendes).
     */
    public record TradeListing(ItemStack cost, ItemStack result, int maxUses, int xp)
            implements VillagerTrades.ItemListing {

        @Override
        public net.minecraft.world.item.trading.MerchantOffer getOffer(
                net.minecraft.world.entity.Entity entity,
                net.minecraft.util.RandomSource random) {
            return new net.minecraft.world.item.trading.MerchantOffer(
                    cost, result, maxUses, xp, 0.05F);
        }
    }

    /** Listing con dos costes (ej: esmeraldas + diamante -> TNT de Diamante). */
    public record TradeListing2(ItemStack costA, ItemStack costB, ItemStack result,
                                int maxUses, int xp) implements VillagerTrades.ItemListing {

        @Override
        public net.minecraft.world.item.trading.MerchantOffer getOffer(
                net.minecraft.world.entity.Entity entity,
                net.minecraft.util.RandomSource random) {
            return new net.minecraft.world.item.trading.MerchantOffer(
                    costA, costB, result, maxUses, xp, 0.05F);
        }
    }
}
