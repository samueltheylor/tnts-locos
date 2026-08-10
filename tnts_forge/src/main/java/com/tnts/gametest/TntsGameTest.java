package com.tnts.gametest;

import com.tnts.ModBlocks;
import com.tnts.ModItems;
import com.tnts.block.TntBlock;
import com.tnts.entity.TntsPrimedTnt;
import com.tnts.item.TntPickaxeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("tnts")
@PrefixGameTestTemplate(false)
public class TntsGameTest {

    /** Una TNT encendida debe convertirse en entidad y explotar al pasar la mecha. */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void mini_tnt_explodes(GameTestHelper helper) {
        BlockPos pos = new BlockPos(8, 2, 8);
        helper.setBlock(pos, ModBlocks.MINI_TNT.get());
        TntBlock block = (TntBlock) helper.getBlockState(pos).getBlock();
        // Ojo: las operaciones directas sobre el nivel necesitan coordenadas ABSOLUTAS
        block.prime(helper.getLevel(), helper.absolutePos(pos), helper.getBlockState(pos), null);
        helper.runAfterDelay(100, () -> {
            helper.assertTrue(helper.getBlockState(pos).isAir(), "La TNT deberia haber explotado");
            AABB area = new AABB(helper.absolutePos(pos)).inflate(4);
            helper.assertTrue(helper.getLevel().getEntitiesOfClass(TntsPrimedTnt.class, area).isEmpty(),
                    "La entidad deberia haber explotado al terminar la mecha");
            helper.succeed();
        });
    }

    /** Solo se enciende con mechero o carga de fuego (con un palo no). */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void only_ignites_with_flint(GameTestHelper helper) {
        BlockPos pos = new BlockPos(8, 2, 8);
        helper.setBlock(pos, ModBlocks.MINI_TNT.get());
        TntBlock block = (TntBlock) helper.getBlockState(pos).getBlock();
        BlockHitResult hit = new BlockHitResult(new Vec3(8.5, 2.5, 8.5), Direction.UP, pos, false);
        AABB area = new AABB(helper.absolutePos(pos)).inflate(3);

        // con un palo NO se enciende
        var player = helper.makeMockPlayer();
        player.getInventory().setItem(player.getInventory().selected, new ItemStack(Items.STICK));
        InteractionResult result = block.use(helper.getBlockState(pos), helper.getLevel(), helper.absolutePos(pos),
                player, InteractionHand.MAIN_HAND, hit);
        helper.assertTrue(result == InteractionResult.PASS, "Con un palo no deberia encenderse");
        helper.assertTrue(helper.getBlockState(pos).getBlock() instanceof TntBlock, "La TNT deberia seguir ahi");
        helper.assertTrue(helper.getLevel().getEntitiesOfClass(TntsPrimedTnt.class, area).isEmpty(),
                "No deberia haber TNT encendida");

        // con mechero SI se enciende: el bloque desaparece y aparece la entidad
        player.getInventory().setItem(player.getInventory().selected, new ItemStack(Items.FLINT_AND_STEEL));
        block.use(helper.getBlockState(pos), helper.getLevel(), helper.absolutePos(pos),
                player, InteractionHand.MAIN_HAND, hit);
        helper.assertTrue(helper.getBlockState(pos).isAir(), "Con mechero la TNT deberia despegar");
        helper.assertTrue(!helper.getLevel().getEntitiesOfClass(TntsPrimedTnt.class, area).isEmpty(),
                "Deberia haberse spawnado la TNT encendida");
        helper.succeed();
    }

    /** Peto de TNT: al recibir dano, empuja al atacante lejos. */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void chestplate_knocks_attacker(GameTestHelper helper) {
        var wearer = helper.makeMockPlayer();
        var attacker = helper.makeMockPlayer();
        BlockPos wPos = new BlockPos(8, 2, 8);
        BlockPos aPos = new BlockPos(10, 2, 8);
        wearer.setPos(helper.absolutePos(wPos).getCenter());
        attacker.setPos(helper.absolutePos(aPos).getCenter());
        wearer.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.TNT_CHESTPLATE.get()));

        // el atacante golpea a quien lleva el peto -> sale empujado hacia -x
        wearer.hurt(helper.getLevel().damageSources().playerAttack(attacker), 5.0F);
        helper.assertTrue(attacker.getDeltaMovement().x < 0,
                "El atacante deberia ser empujado lejos del peto");
        helper.succeed();
    }

    /** Pico de TNT: cada bloque roto explota (el bloque desaparece). */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void pickaxe_explodes_mined_block(GameTestHelper helper) {
        BlockPos pos = new BlockPos(8, 2, 8);
        helper.setBlock(pos, Blocks.STONE);
        var player = helper.makeMockPlayer();
        ItemStack pick = new ItemStack(ModItems.TNT_PICKAXE.get());
        TntPickaxeItem pickaxe = (TntPickaxeItem) pick.getItem();
        pickaxe.mineBlock(pick, helper.getLevel(), helper.getBlockState(pos),
                helper.absolutePos(pos), player);
        helper.assertTrue(helper.getBlockState(pos).isAir(),
                "El bloque minado deberia haber explotado");
        helper.succeed();
    }

    /** Lanzador: consume una TNT del inventario y dispara una TNT encendida. */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void launcher_consumes_tnt_and_fires(GameTestHelper helper) {
        var player = helper.makeMockPlayer();
        BlockPos pos = new BlockPos(8, 2, 8);
        player.setPos(helper.absolutePos(pos).getCenter());
        // ojo: setItemInHand ocupa el slot seleccionado (0), asi que la TNT va en el 1
        player.getInventory().setItem(1, new ItemStack(ModItems.MINI_TNT.get(), 3));
        ItemStack launcher = new ItemStack(ModItems.LAUNCHER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, launcher);

        InteractionResultHolder<ItemStack> result =
                launcher.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(result.getResult() == InteractionResult.SUCCESS, "Deberia disparar");
        helper.assertTrue(player.getInventory().getItem(1).getCount() == 2,
                "Deberia consumir una TNT del inventario");
        // la entidad entra en el nivel al flush del entity manager: comprobar un tick despues
        helper.runAfterDelay(2, () -> {
            AABB area = new AABB(helper.absolutePos(pos)).inflate(10);
            helper.assertTrue(!helper.getLevel().getEntitiesOfClass(TntsPrimedTnt.class, area).isEmpty(),
                    "Deberia haber una TNT volando");
            helper.succeed();
        });
    }

    /** Granada: se lanza y consume una unidad. */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void grenade_spawns_primed_tnt(GameTestHelper helper) {
        var player = helper.makeMockPlayer();
        BlockPos pos = new BlockPos(8, 2, 8);
        player.setPos(helper.absolutePos(pos).getCenter());
        ItemStack grenade = new ItemStack(ModItems.GRENADE.get(), 5);
        player.setItemInHand(InteractionHand.MAIN_HAND, grenade);

        grenade.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(player.getMainHandItem().getCount() == 4,
                "Deberia consumir una granada");
        // la entidad entra en el nivel al flush del entity manager: comprobar un tick despues
        helper.runAfterDelay(2, () -> {
            AABB area = new AABB(helper.absolutePos(pos)).inflate(10);
            helper.assertTrue(!helper.getLevel().getEntitiesOfClass(TntsPrimedTnt.class, area).isEmpty(),
                    "Deberia haber una granada volando");
            helper.succeed();
        });
    }

    /** La TNT encendida NO se mueve de su sitio (ni cae ni la empujan). */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void primed_tnt_stays_in_place(GameTestHelper helper) {
        // posicion flotando: 4 bloques por encima del suelo del template
        BlockPos pos = new BlockPos(8, 4, 8);
        helper.setBlock(pos, ModBlocks.MINI_TNT.get());
        TntBlock block = (TntBlock) helper.getBlockState(pos).getBlock();
        block.prime(helper.getLevel(), helper.absolutePos(pos), helper.getBlockState(pos), null);
        AABB area = new AABB(helper.absolutePos(pos)).inflate(1);
        Vec3 origin = helper.absolutePos(pos).getCenter();

        helper.runAfterDelay(15, () -> {
            var tnts = helper.getLevel().getEntitiesOfClass(TntsPrimedTnt.class, area);
            helper.assertTrue(!tnts.isEmpty(), "Deberia existir la TNT encendida");
            if (!tnts.isEmpty()) {
                helper.assertTrue(tnts.get(0).position().distanceToSqr(origin) < 0.5,
                        "La TNT encendida no deberia moverse de su sitio (ni caer)");
            }
            helper.succeed();
        });
    }

    /** Agujero Negro: explota y su crater succiona 3s sin errores. */
    @GameTest(template = "empty", timeoutTicks = 300)
    public static void blackhole_explodes_and_sucks(GameTestHelper helper) {
        BlockPos pos = new BlockPos(8, 2, 8);
        helper.setBlock(pos, ModBlocks.NEGRA_TNT.get());
        TntBlock block = (TntBlock) helper.getBlockState(pos).getBlock();
        block.prime(helper.getLevel(), helper.absolutePos(pos), helper.getBlockState(pos), null);
        AABB area = new AABB(helper.absolutePos(pos)).inflate(3);

        // mecha (40) + succion persistente (60) + margen: la entidad ya no existe
        helper.runAfterDelay(115, () -> {
            helper.assertTrue(helper.getBlockState(pos).isAir(), "La TNT deberia haber explotado");
            helper.assertTrue(helper.getLevel().getEntitiesOfClass(TntsPrimedTnt.class, area).isEmpty(),
                    "La entidad deberia haber explotado al terminar la mecha");
            helper.succeed();
        });
    }

    /** TNTs masivas: Terremoto y Colosal explotan sin crashear (regresion del NPE). */
    @GameTest(template = "empty", timeoutTicks = 300)
    public static void massive_tnts_do_not_crash(GameTestHelper helper) {
        BlockPos t1 = new BlockPos(6, 2, 6);
        BlockPos t2 = new BlockPos(10, 2, 10);
        helper.setBlock(t1, ModBlocks.TERREMOTO_TNT.get());
        helper.setBlock(t2, ModBlocks.COLOSAL_TNT.get());
        TntBlock b1 = (TntBlock) helper.getBlockState(t1).getBlock();
        TntBlock b2 = (TntBlock) helper.getBlockState(t2).getBlock();
        b1.prime(helper.getLevel(), helper.absolutePos(t1), helper.getBlockState(t1), null);
        b2.prime(helper.getLevel(), helper.absolutePos(t2), helper.getBlockState(t2), null);

        // mecha (60/70) + oleadas del colosal (24 ticks extra) + margen
        helper.runAfterDelay(140, () -> {
            helper.assertTrue(helper.getBlockState(t1).isAir(), "La Terremoto deberia haber explotado");
            helper.assertTrue(helper.getBlockState(t2).isAir(), "La Colosal deberia haber explotado");
            helper.succeed();
        });
    }

    /** Detonador: con 12 TNTs alrededor debe encender 10 o mas de golpe. */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void detonator_ignites_many(GameTestHelper helper) {
        for (int i = 0; i < 12; i++) {
            helper.setBlock(new BlockPos(8 + i, 2, 8), ModBlocks.MINI_TNT.get());
        }
        var player = helper.makeMockPlayer();
        BlockPos pos = new BlockPos(8, 2, 8);
        player.setPos(helper.absolutePos(pos).getCenter());
        ItemStack detonator = new ItemStack(ModItems.DETONATOR.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, detonator);

        detonator.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        AABB area = new AABB(helper.absolutePos(new BlockPos(8, 2, 8)),
                helper.absolutePos(new BlockPos(19, 2, 8))).inflate(3);
        helper.assertTrue(helper.getLevel().getEntitiesOfClass(TntsPrimedTnt.class, area).size() >= 10,
                "El detonador deberia encender 10+ TNTs de golpe");
        helper.succeed();
    }

    /** El bunker de TNT esta registrado (datapack: estructura + structure set). */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void bunker_structure_registered(GameTestHelper helper) {
        RegistryAccess access = helper.getLevel().registryAccess();
        Registry<Structure> structures = access.registryOrThrow(Registries.STRUCTURE);
        Registry<StructureSet> sets = access.registryOrThrow(Registries.STRUCTURE_SET);
        helper.assertTrue(structures.containsKey(new ResourceLocation("tnts", "tnts_bunker")),
                "La estructura tnts_bunker deberia estar registrada");
        helper.assertTrue(sets.containsKey(new ResourceLocation("tnts", "tnts_bunkers")),
                "El structure set tnts_bunkers deberia estar registrado");
        helper.succeed();
    }
}
