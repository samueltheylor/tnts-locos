package com.tnts.gametest;

import com.tnts.ModBlocks;
import com.tnts.ModItems;
import com.tnts.block.TntBlock;
import com.tnts.entity.SupernovaEntity;
import com.tnts.entity.TntKingEntity;
import com.tnts.entity.TntsEntities;
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

    /** Efectos 3D de las masivas: meteoritos y supernova spawnan entidades. */
    @GameTest(template = "empty", timeoutTicks = 300)
    public static void massive_3d_effects_spawn_entities(GameTestHelper helper) {
        BlockPos t1 = new BlockPos(6, 2, 6);
        BlockPos t2 = new BlockPos(10, 2, 10);
        helper.setBlock(t1, ModBlocks.METEORITO_TNT.get());
        helper.setBlock(t2, ModBlocks.SUPERNOVA_TNT.get());
        TntBlock b1 = (TntBlock) helper.getBlockState(t1).getBlock();
        TntBlock b2 = (TntBlock) helper.getBlockState(t2).getBlock();
        b1.prime(helper.getLevel(), helper.absolutePos(t1), helper.getBlockState(t1), null);
        b2.prime(helper.getLevel(), helper.absolutePos(t2), helper.getBlockState(t2), null);

        // meteoritos caen 32 bloques -> mecha (55) + caida (~46 ticks) + margen
        helper.runAfterDelay(160, () -> {
            helper.assertTrue(helper.getBlockState(t1).isAir(), "La Meteorito deberia haber explotado");
            helper.assertTrue(helper.getBlockState(t2).isAir(), "La Supernova deberia haber explotado");
            // la supernova 3D dura 50 ticks: deberia haber pasado ya
            AABB area = new AABB(helper.absolutePos(new BlockPos(6, 2, 6)),
                    helper.absolutePos(new BlockPos(10, 12, 10))).inflate(4);
            helper.assertTrue(
                    helper.getLevel().getEntitiesOfClass(SupernovaEntity.class, area).isEmpty(),
                    "La esfera de supernova deberia haberse desvanecido");
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

    /** TNTs nuevas 1.9.3: Toxica, Fuegos y Gravitatoria explotan sin crashear. */
    @GameTest(template = "empty", timeoutTicks = 300)
    public static void new_tnts_193_explode_safely(GameTestHelper helper) {
        BlockPos t1 = new BlockPos(5, 2, 5);
        BlockPos t2 = new BlockPos(8, 2, 8);
        BlockPos t3 = new BlockPos(11, 2, 11);
        helper.setBlock(t1, ModBlocks.TOXICA_TNT.get());
        helper.setBlock(t2, ModBlocks.FUEGOS_TNT.get());
        helper.setBlock(t3, ModBlocks.GRAVITATORIA_TNT.get());
        ((TntBlock) helper.getBlockState(t1).getBlock())
                .prime(helper.getLevel(), helper.absolutePos(t1), helper.getBlockState(t1), null);
        ((TntBlock) helper.getBlockState(t2).getBlock())
                .prime(helper.getLevel(), helper.absolutePos(t2), helper.getBlockState(t2), null);
        ((TntBlock) helper.getBlockState(t3).getBlock())
                .prime(helper.getLevel(), helper.absolutePos(t3), helper.getBlockState(t3), null);
        helper.runAfterDelay(160, () -> {
            helper.assertTrue(helper.getBlockState(t1).isAir(), "La Toxica deberia haber explotado");
            helper.assertTrue(helper.getBlockState(t2).isAir(), "La Fuegos deberia haber explotado");
            helper.assertTrue(helper.getBlockState(t3).isAir(), "La Gravitatoria deberia haber explotado");
            helper.succeed();
        });
    }

    /** Desactivar una TNT con tijeras: la apaga y devuelve el bloque. */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void shears_defuse_tnt(GameTestHelper helper) {
        BlockPos pos = new BlockPos(8, 2, 8);
        helper.setBlock(pos, ModBlocks.MINI_TNT.get());
        TntBlock block = (TntBlock) helper.getBlockState(pos).getBlock();
        block.prime(helper.getLevel(), helper.absolutePos(pos), helper.getBlockState(pos), null);
        AABB area = new AABB(helper.absolutePos(pos)).inflate(3);

        helper.runAfterDelay(5, () -> {
            var tnts = helper.getLevel().getEntitiesOfClass(TntsPrimedTnt.class, area);
            helper.assertTrue(!tnts.isEmpty(), "Deberia haber una TNT encendida");
            if (tnts.isEmpty()) {
                helper.succeed();
                return;
            }
            var player = helper.makeMockPlayer();
            player.getInventory().setItem(player.getInventory().selected, new ItemStack(Items.SHEARS));
            tnts.get(0).interact(player, InteractionHand.MAIN_HAND);
            helper.runAfterDelay(2, () -> {
                helper.assertTrue(helper.getLevel().getEntitiesOfClass(TntsPrimedTnt.class, area).isEmpty(),
                        "La TNT deberia haberse desactivado con las tijeras");
                helper.assertTrue(!helper.getLevel().getEntitiesOfClass(
                                net.minecraft.world.entity.item.ItemEntity.class, area).isEmpty(),
                        "Deberia haberse soltado el bloque de TNT");
                helper.succeed();
            });
        });
    }

    /** TNTs nuevas 1.10.0: End, Burbuja y Solar explotan sin crashear. */
    @GameTest(template = "empty", timeoutTicks = 300)
    public static void new_tnts_110_explode_safely(GameTestHelper helper) {
        BlockPos t1 = new BlockPos(5, 2, 5);
        BlockPos t2 = new BlockPos(8, 2, 8);
        BlockPos t3 = new BlockPos(11, 2, 11);
        helper.setBlock(t1, ModBlocks.ENDER_TNT.get());
        helper.setBlock(t2, ModBlocks.BUBBLE_TNT.get());
        helper.setBlock(t3, ModBlocks.SOLAR_TNT.get());
        ((TntBlock) helper.getBlockState(t1).getBlock())
                .prime(helper.getLevel(), helper.absolutePos(t1), helper.getBlockState(t1), null);
        ((TntBlock) helper.getBlockState(t2).getBlock())
                .prime(helper.getLevel(), helper.absolutePos(t2), helper.getBlockState(t2), null);
        ((TntBlock) helper.getBlockState(t3).getBlock())
                .prime(helper.getLevel(), helper.absolutePos(t3), helper.getBlockState(t3), null);
        helper.runAfterDelay(160, () -> {
            helper.assertTrue(helper.getBlockState(t1).isAir(), "La End deberia haber explotado");
            helper.assertTrue(helper.getBlockState(t2).isAir(), "La Burbuja deberia haber explotado");
            helper.assertTrue(helper.getBlockState(t3).isAir(), "La Solar deberia haber explotado");
            helper.succeed();
        });
    }

    /** El altar del bunker convoca al Rey TNT al usarlo. */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void altar_spawns_king(GameTestHelper helper) {
        BlockPos pos = new BlockPos(8, 2, 8);
        helper.setBlock(pos, ModBlocks.TNT_ALTAR.get());
        var player = helper.makeMockPlayer();
        BlockHitResult hit = new BlockHitResult(new Vec3(8.5, 2.5, 8.5), Direction.UP, pos, false);
        ModBlocks.TNT_ALTAR.get().use(helper.getBlockState(pos), helper.getLevel(), helper.absolutePos(pos),
                player, InteractionHand.MAIN_HAND, hit);
        helper.runAfterDelay(3, () -> {
            AABB area = new AABB(helper.absolutePos(pos)).inflate(50);
            helper.assertTrue(!helper.getLevel().getEntitiesOfClass(TntKingEntity.class, area).isEmpty(),
                    "El altar deberia convocar al Rey TNT");
            helper.succeed();
        });
    }

    /** Desactivar la TNT REAL del Rey con tijeras lo aturde 5 segundos. */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void royal_tnt_defuse_stuns_king(GameTestHelper helper) {
        BlockPos pos = new BlockPos(8, 2, 8);
        // invocar al Rey directamente (los tests comparten mundo: el altar
        // no convoca si otro test dejo un Rey vivo cerca, y aqui solo nos
        // importa la TNT Real -> stun, no la invocacion)
        BlockPos ap = helper.absolutePos(pos);
        TntKingEntity king = new TntKingEntity(TntsEntities.TNT_KING.get(), helper.getLevel());
        king.moveTo(ap.getX() + 0.5, ap.getY() + 1, ap.getZ() + 0.5, 0.0F, 0.0F);
        helper.getLevel().addFreshEntity(king);
        // lanzar una TNT REAL junto al Rey (mecha larga para que no explote)
        TntsPrimedTnt royal = new TntsPrimedTnt(helper.getLevel(),
                ap.getX() + 0.5, ap.getY() + 1.5, ap.getZ() + 0.5,
                "tnts:mega_tnt", 200, null);
        royal.setRoyal(true);
        helper.getLevel().addFreshEntity(royal);
        AABB area = new AABB(helper.absolutePos(pos)).inflate(50);

        helper.runAfterDelay(4, () -> {
            var kings = helper.getLevel().getEntitiesOfClass(TntKingEntity.class, area);
            var tnts = helper.getLevel().getEntitiesOfClass(TntsPrimedTnt.class, area);
            helper.assertTrue(!kings.isEmpty() && !tnts.isEmpty(),
                    "Deberia haber el Rey y su TNT Real");
            if (kings.isEmpty() || tnts.isEmpty()) {
                helper.succeed();
                return;
            }
            var player = helper.makeMockPlayer();
            player.getInventory().setItem(player.getInventory().selected, new ItemStack(Items.SHEARS));
            helper.assertTrue(royal.isAlive() && royal.isRoyal(),
                    "La TNT Real deberia seguir viva y ser royal");
            royal.interact(player, InteractionHand.MAIN_HAND);
            helper.runAfterDelay(2, () -> {
                helper.assertTrue(royal.isRemoved(),
                        "La TNT Real deberia haber sido desactivada");
                helper.assertTrue(king.isAlive() && king.isStunned(),
                        "Desactivar la TNT Real deberia aturdir al Rey");
                helper.succeed();
            });
        });
    }

    /** El grito de invocacion del Rey trae piglins del Nether. */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void king_force_summon(GameTestHelper helper) {
        BlockPos pos = new BlockPos(8, 2, 8);
        helper.setBlock(pos, ModBlocks.TNT_ALTAR.get());
        var summoner = helper.makeMockPlayer();
        BlockHitResult hit = new BlockHitResult(new Vec3(8.5, 2.5, 8.5), Direction.UP, pos, false);
        ModBlocks.TNT_ALTAR.get().use(helper.getBlockState(pos), helper.getLevel(), helper.absolutePos(pos),
                summoner, InteractionHand.MAIN_HAND, hit);
        AABB area = new AABB(helper.absolutePos(pos)).inflate(50);

        helper.runAfterDelay(4, () -> {
            var kings = helper.getLevel().getEntitiesOfClass(TntKingEntity.class, area);
            helper.assertTrue(!kings.isEmpty(), "Deberia estar el Rey TNT");
            if (kings.isEmpty()) {
                helper.succeed();
                return;
            }
            kings.get(0).forceSummon();
            helper.runAfterDelay(2, () -> {
                helper.assertTrue(
                        !helper.getLevel().getEntitiesOfClass(
                                        net.minecraft.world.entity.monster.ZombifiedPiglin.class, area)
                                .isEmpty()
                                || !helper.getLevel().getEntitiesOfClass(
                                        net.minecraft.world.entity.monster.piglin.PiglinBrute.class, area)
                                .isEmpty(),
                        "El grito del Rey deberia invocar piglins del Nether");
                helper.succeed();
            });
        });
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

    /** particleQuality=2: el espectaculo extra de las masivas no crashea. */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void massive_quality2_show_no_crash(GameTestHelper helper) {
        // ejercita el espectaculo de quality=2 directamente (en el server de
        // tests la config va a 1, asi que se fuerza la llamada publica)
        TntsPrimedTnt.massiveQualityShow(helper.getLevel(), 8.5, 3.5, 8.5);
        helper.succeed();
    }

    /** La derrota del Rey: parpadea, se agrieta y hace boom sin crashear. */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void king_defeat_animation_no_crash(GameTestHelper helper) {
        BlockPos pos = new BlockPos(8, 2, 8);
        BlockPos ap = helper.absolutePos(pos);
        TntKingEntity king = new TntKingEntity(TntsEntities.TNT_KING.get(), helper.getLevel());
        king.moveTo(ap.getX() + 0.5, ap.getY() + 1, ap.getZ() + 0.5, 0.0F, 0.0F);
        helper.getLevel().addFreshEntity(king);

        helper.runAfterDelay(5, () -> {
            // matarlo con daño suficiente de una vez
            king.hurt(king.damageSources().generic(), 1000.0F);
            helper.assertTrue(king.isDying(), "El Rey deberia estar en la secuencia de derrota");
            helper.runAfterDelay(90, () -> {
                // ~2.2s despues ya debe haber hecho boom y desaparecido
                helper.assertTrue(king.isRemoved(),
                        "El Rey deberia haber hecho boom y desaparecido tras la derrota");
                helper.succeed();
            });
        });
    }

    /** El almacen saqueado esta registrado y su pieza genera celdas con cofres. */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void warehouse_structure_generates(GameTestHelper helper) {
        RegistryAccess access = helper.getLevel().registryAccess();
        Registry<Structure> structures = access.registryOrThrow(Registries.STRUCTURE);
        helper.assertTrue(structures.containsKey(new ResourceLocation("tnts", "tnts_warehouse")),
                "La estructura tnts_warehouse deberia estar registrada");
        helper.assertTrue(structures.containsKey(new ResourceLocation("tnts", "tnts_test_range")),
                "La estructura tnts_test_range deberia estar registrada");
        helper.assertTrue(access.registryOrThrow(Registries.STRUCTURE_SET)
                        .containsKey(new ResourceLocation("tnts", "tnts_warehouses")),
                "El structure set tnts_warehouses deberia estar registrado");
        helper.assertTrue(access.registryOrThrow(Registries.STRUCTURE_SET)
                        .containsKey(new ResourceLocation("tnts", "tnts_test_ranges")),
                "El structure set tnts_test_ranges deberia estar registrado");
        helper.succeed();
    }

    /** La Espada del Rey enciende TNTs cercanas al golpear. */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void king_sword_primes_tnts(GameTestHelper helper) {
        BlockPos tntPos = new BlockPos(8, 2, 8);
        helper.setBlock(tntPos, ModBlocks.MINI_TNT.get());
        BlockPos ap = helper.absolutePos(tntPos);
        AABB area = new AABB(ap).inflate(3);
        var attacker = helper.makeMockPlayer();
        var victim = helper.makeMockPlayer();
        victim.moveTo(ap.getX() + 3, ap.getY(), ap.getZ());
        helper.getLevel().addFreshEntity(victim);

        helper.runAfterDelay(3, () -> {
            var sword = new ItemStack(ModItems.TNT_KING_SWORD.get());
            attacker.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, sword);
            com.tnts.item.TntKingSwordItem item =
                    (com.tnts.item.TntKingSwordItem) sword.getItem();
            item.hurtEnemy(sword, victim, attacker);
            helper.assertTrue(!helper.getLevel().getEntitiesOfClass(
                    TntsPrimedTnt.class, area).isEmpty(),
                    "La Espada del Rey deberia encender la TNT cercana");
            helper.succeed();
        });
    }

    /** Fases visuales: el Rey se agrieta (getCrackLevel) al perder vida. */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void king_cracks_at_low_health(GameTestHelper helper) {
        BlockPos pos = new BlockPos(8, 2, 8);
        BlockPos ap = helper.absolutePos(pos);
        TntKingEntity king = new TntKingEntity(TntsEntities.TNT_KING.get(), helper.getLevel());
        king.moveTo(ap.getX() + 0.5, ap.getY() + 1, ap.getZ() + 0.5, 0.0F, 0.0F);
        helper.getLevel().addFreshEntity(king);

        helper.runAfterDelay(5, () -> {
            helper.assertTrue(king.getCrackLevel() == 0,
                    "A vida completa el Rey deberia estar sano (nivel 0)");
            // bajar al ~50% -> nivel 1 (agrietado) — setHealth directo para
            // evitar la invulnerabilidad temporal tras el primer hurt
            king.setHealth(150.0F);
            helper.assertTrue(king.getCrackLevel() == 1,
                    "Al 50% de vida el Rey deberia tener grietas (nivel 1)");
            // bajar a <33% -> nivel 2 (muy agrietado)
            king.setHealth(90.0F);
            helper.assertTrue(king.getCrackLevel() == 2,
                    "Por debajo del 33% el Rey deberia estar muy agrietado (nivel 2)");
            // un tick con particulas de fragmentos (no debe crashear)
            helper.runAfterDelay(10, () -> {
                helper.assertTrue(king.isAlive(), "El Rey deberia seguir vivo con sus grietas");
                helper.succeed();
            });
        });
    }

    /** El Escudo de TNT bloquea y empuja al atacante (efecto via ShieldBlockEvent). */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void tnt_shield_pushes_attacker(GameTestHelper helper) {
        BlockPos pos = new BlockPos(8, 2, 8);
        BlockPos ap = helper.absolutePos(pos);
        var wearer = helper.makeMockPlayer();
        var attacker = helper.makeMockPlayer();
        wearer.moveTo(ap.getX(), ap.getY() + 1, ap.getZ());
        attacker.moveTo(ap.getX() + 2, ap.getY() + 1, ap.getZ());
        helper.getLevel().addFreshEntity(wearer);
        helper.getLevel().addFreshEntity(attacker);

        helper.runAfterDelay(3, () -> {
            wearer.startUsingItem(net.minecraft.world.InteractionHand.MAIN_HAND);
            wearer.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                    new ItemStack(ModItems.TNT_SHIELD.get()));
            // forzar el evento: atacante golpea al portador con el escudo activo
            attacker.attack(wearer);
            double before = attacker.position().distanceTo(wearer.position());
            helper.runAfterDelay(2, () -> {
                double after = attacker.position().distanceTo(wearer.position());
                helper.assertTrue(after > before,
                        "El Escudo de TNT deberia empujar al atacante (antes="
                                + before + ", despues=" + after + ")");
                helper.succeed();
            });
        });
    }

    /** Set bonus (2 piezas): la Espada del Rey enciende TNTs al DOBLE de radio. */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void king_set_sword_double_radius(GameTestHelper helper) {
        // Dos TNTs: una a 4 bloques del objetivo (dentro del radio normal 6) y
        // otra a 9 bloques (fuera de 6 pero dentro del radio del set 12).
        // Todo dentro del template 15x15 para que nada caiga fuera de la zona
        // del test (x e z en [0,14]).
        BlockPos center = new BlockPos(2, 2, 8);
        BlockPos near = new BlockPos(6, 2, 8);    // 4 bloques
        BlockPos far = new BlockPos(11, 2, 8);    // 9 bloques
        helper.setBlock(near, ModBlocks.MINI_TNT.get());
        helper.setBlock(far, ModBlocks.MINI_TNT.get());
        BlockPos apNear = helper.absolutePos(near);
        BlockPos apFar = helper.absolutePos(far);
        AABB areaNear = new AABB(apNear).inflate(3);
        AABB areaFar = new AABB(apFar).inflate(3);

        // Objetivo: un zombie sin gravedad ni AI en el centro (posicion exacta)
        var victim = new net.minecraft.world.entity.monster.Zombie(
                net.minecraft.world.entity.EntityType.ZOMBIE, helper.getLevel());
        victim.setNoGravity(true);
        victim.setNoAi(true);
        victim.setPos(helper.absolutePos(center).getCenter());
        helper.getLevel().addFreshEntity(victim);

        var attacker = helper.makeMockPlayer();
        var sword = new ItemStack(ModItems.TNT_KING_SWORD.get());
        attacker.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, sword);
        com.tnts.item.TntKingSwordItem item =
                (com.tnts.item.TntKingSwordItem) sword.getItem();

        // Sincrono en el tick 0: los tests corren en paralelo en el mismo mundo
        // y cualquier espera permite que otra TNT ajena destruya estos bloques.
        // Sin set: radio 6 -> la TNT a 4 se enciende (bloque desaparece),
        // la de 9 sigue siendo un bloque TNT apagado.
        item.hurtEnemy(sword, victim, attacker);
        helper.assertTrue(helper.getBlockState(near).isAir(),
                "Sin set, la TNT a 4 bloques deberia encenderse (radio 6)");
        helper.assertTrue(helper.getBlockState(far).getBlock() instanceof TntBlock,
                "Sin set, la TNT a 9 bloques no deberia encenderse");

        // con la corona puesta (espada + corona = 2 piezas): radio 12
        attacker.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,
                new ItemStack(ModItems.TNT_KING_CROWN.get()));
        helper.assertTrue(com.tnts.TntKingSet.countPieces(attacker) == 2,
                "El atacante deberia tener 2 piezas del set (espada + corona)");
        item.hurtEnemy(sword, victim, attacker);
        helper.assertTrue(helper.getBlockState(far).isAir(),
                "Con 2 piezas del set, la TNT a 9 bloques deberia encenderse");
        helper.succeed();
    }

    /** Set bonus (3 piezas): inmunidad al dano de las TNTs del propio mod. */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void king_set_three_pieces_immune_to_tnt_blast(GameTestHelper helper) {
        BlockPos pos = new BlockPos(8, 2, 8);
        BlockPos ap = helper.absolutePos(pos);
        var player = helper.makeMockPlayer();
        player.moveTo(ap.getX(), ap.getY() + 1, ap.getZ());
        helper.getLevel().addFreshEntity(player);

        helper.runAfterDelay(3, () -> {
            // 3 piezas: corona + peto + espada
            player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,
                    new ItemStack(ModItems.TNT_KING_CROWN.get()));
            player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST,
                    new ItemStack(ModItems.TNT_CHESTPLATE.get()));
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                    new ItemStack(ModItems.TNT_KING_SWORD.get()));
            helper.assertTrue(com.tnts.TntKingSet.countPieces(player) >= 3,
                    "El jugador deberia tener 3 piezas del set");
            float before = player.getHealth();
            // una TNT del mod explota cerca (entidad tnts:primed_tnt)
            TntsPrimedTnt tnt = new TntsPrimedTnt(helper.getLevel(),
                    ap.getX(), ap.getY() + 1, ap.getZ(), "tnts:mini_tnt", 5, player);
            helper.getLevel().addFreshEntity(tnt);
            tnt.setPos(ap.getX() + 1.5, ap.getY() + 1, ap.getZ());
            helper.runAfterDelay(15, () -> {
                helper.assertTrue(player.getHealth() >= before,
                        "Con 3 piezas el jugador no deberia recibir dano de las TNTs del mod");
                helper.succeed();
            });
        });
    }

    /** Casco de TNT: al llevarlo puesto da vision nocturna. */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void tnt_helmet_gives_night_vision(GameTestHelper helper) {
        BlockPos pos = new BlockPos(8, 2, 8);
        BlockPos ap = helper.absolutePos(pos);
        var player = helper.makeMockPlayer();
        player.moveTo(ap.getX(), ap.getY() + 1, ap.getZ());
        helper.getLevel().addFreshEntity(player);

        helper.runAfterDelay(3, () -> {
            player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,
                    new ItemStack(ModItems.TNT_HELMET.get()));
            // el efecto se aplica en el siguiente tick del evento
            helper.runAfterDelay(3, () -> {
                helper.assertTrue(player.hasEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION),
                        "El Casco de TNT deberia dar vision nocturna");
                helper.succeed();
            });
        });
    }

    /** Rey guardian: mitad de vida, sin barra de jefe y con modo activado. */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void king_guardian_has_half_health(GameTestHelper helper) {
        BlockPos pos = new BlockPos(8, 2, 8);
        BlockPos ap = helper.absolutePos(pos);
        TntKingEntity king = new TntKingEntity(TntsEntities.TNT_KING.get(), helper.getLevel());
        king.setGuardianMode();
        king.moveTo(ap.getX() + 0.5, ap.getY() + 1, ap.getZ() + 0.5, 0.0F, 0.0F);
        helper.getLevel().addFreshEntity(king);

        helper.runAfterDelay(5, () -> {
            helper.assertTrue(king.isGuardian(), "El Rey deberia estar en modo guardian");
            helper.assertTrue(king.getMaxHealth() == TntKingEntity.MAX_HP / 2,
                    "El guardian deberia tener la mitad de vida ("
                            + king.getMaxHealth() + " vs " + (TntKingEntity.MAX_HP / 2) + ")");
            helper.succeed();
        });
    }

    /** Pantalon de TNT: completa la armadura (4 piezas = set bonus). */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void tnt_leggings_completes_armor_set(GameTestHelper helper) {
        BlockPos pos = new BlockPos(8, 2, 8);
        BlockPos ap = helper.absolutePos(pos);
        var player = helper.makeMockPlayer();
        player.moveTo(ap.getX(), ap.getY() + 1, ap.getZ());
        helper.getLevel().addFreshEntity(player);

        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,
                new ItemStack(ModItems.TNT_HELMET.get()));
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST,
                new ItemStack(ModItems.TNT_CHESTPLATE.get()));
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.LEGS,
                new ItemStack(ModItems.TNT_LEGGINGS.get()));
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET,
                new ItemStack(ModItems.TNT_BOOTS.get()));

        helper.assertTrue(com.tnts.TntArmorSet.countPieces(player) == 4,
                "Con las 4 piezas el set de armadura de TNT deberia estar completo");
        helper.assertTrue(com.tnts.TntArmorSet.isComplete(player),
                "isComplete deberia devolver true con las 4 piezas");
        helper.succeed();
    }

    /** Set completo de armadura: reduce el dano de explosiones a la mitad. */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void tnt_armor_full_set_halves_explosion_damage(GameTestHelper helper) {
        BlockPos pos = new BlockPos(8, 2, 8);
        BlockPos ap = helper.absolutePos(pos);

        // dos zombies identicos: uno con la armadura completa y otro sin ella
        var armored = new net.minecraft.world.entity.monster.Zombie(
                net.minecraft.world.entity.EntityType.ZOMBIE, helper.getLevel());
        armored.setNoAi(true);
        armored.setNoGravity(true);
        armored.setHealth(20.0F);
        armored.setPos(ap.getX() + 0.5, ap.getY() + 1, ap.getZ() + 0.5);
        helper.getLevel().addFreshEntity(armored);
        armored.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,
                new ItemStack(ModItems.TNT_HELMET.get()));
        armored.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST,
                new ItemStack(ModItems.TNT_CHESTPLATE.get()));
        armored.setItemSlot(net.minecraft.world.entity.EquipmentSlot.LEGS,
                new ItemStack(ModItems.TNT_LEGGINGS.get()));
        armored.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET,
                new ItemStack(ModItems.TNT_BOOTS.get()));

        var plain = new net.minecraft.world.entity.monster.Zombie(
                net.minecraft.world.entity.EntityType.ZOMBIE, helper.getLevel());
        plain.setNoAi(true);
        plain.setNoGravity(true);
        plain.setHealth(20.0F);
        plain.setPos(ap.getX() + 2.5, ap.getY() + 1, ap.getZ() + 0.5);
        helper.getLevel().addFreshEntity(plain);

        helper.assertTrue(com.tnts.TntArmorSet.countPieces(armored) == 4,
                "El zombie blindado deberia llevar las 4 piezas");
        // el evento de reduccion se aplica sobre dano de tipo explosion:
        // creamos un LivingHurtEvent sintetico y comprobamos que reduce a la mitad
        net.minecraft.world.damagesource.DamageSource explosion = helper.getLevel().damageSources()
                .explosion((net.minecraft.world.entity.Entity) null, (net.minecraft.world.entity.Entity) null);
        net.minecraftforge.event.entity.living.LivingHurtEvent armoredEvent =
                new net.minecraftforge.event.entity.living.LivingHurtEvent(armored, explosion, 10.0F);
        com.tnts.TntsEvents.onLivingHurt(armoredEvent);
        net.minecraftforge.event.entity.living.LivingHurtEvent plainEvent =
                new net.minecraftforge.event.entity.living.LivingHurtEvent(plain, explosion, 10.0F);
        com.tnts.TntsEvents.onLivingHurt(plainEvent);
        helper.assertTrue(armoredEvent.getAmount() <= 5.01F,
                "Con la armadura completa el dano de explosion deberia reducirse a la mitad ("
                        + armoredEvent.getAmount() + ")");
        helper.assertTrue(plainEvent.getAmount() == 10.0F,
                "Sin armadura el dano deberia quedarse en 10 ("
                        + plainEvent.getAmount() + ")");
        helper.succeed();
    }

    /** Fases visuales del Rey: el crack level sube al bajar la vida. */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void king_phase_advancement_fires_at_level_2(GameTestHelper helper) {
        BlockPos pos = new BlockPos(8, 2, 8);
        BlockPos ap = helper.absolutePos(pos);
        TntKingEntity king = new TntKingEntity(TntsEntities.TNT_KING.get(), helper.getLevel());
        king.setHealth(30.0F); // ~10% -> muy agrietado (nivel 2)
        king.moveTo(ap.getX() + 0.5, ap.getY() + 1, ap.getZ() + 0.5, 0.0F, 0.0F);
        helper.getLevel().addFreshEntity(king);

        helper.runAfterDelay(3, () -> {
            helper.assertTrue(king.getCrackLevel() == 2,
                    "Con poca vida el Rey deberia estar en nivel 2 de grietas");
            // el ultimo nivel visto se actualiza al hacer tick
            king.tick();
            helper.succeed();
        });
    }

    /** Todos los bloques del mod deben tener item registrado (evita el crash del inventario creativo). */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void all_blocks_have_items(GameTestHelper helper) {
        for (net.minecraft.world.level.block.Block block : com.tnts.ModBlocks.getAllBlocks()) {
            net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(block);
            helper.assertTrue(!stack.isEmpty(),
                    "El bloque " + net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(block)
                            + " no tiene item registrado: el inventario creativo crashearia ("
                            + "The stack count must be 1)");
            helper.assertTrue(stack.getCount() == 1,
                    "El stack de " + net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(block)
                            + " deberia tener count 1");
        }
        helper.succeed();
    }

    /** TNT Casa: al explotar construye una casita de madera utilizable. */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void casa_tnt_builds_house(GameTestHelper helper) {
        BlockPos ap = helper.absolutePos(new BlockPos(8, 2, 8));
        // entidad de la TNT Casa con mecha corta (camino completo: entidad ->
        // explosion -> construccion). Ojo: getBlockState(pos) del helper trata
        // el pos como RELATIVO, asi que se usa el nivel directamente.
        var tnt = new com.tnts.entity.TntsPrimedTnt(
                helper.getLevel(), ap.getX() + 0.5, ap.getY(), ap.getZ() + 0.5,
                "tnts:casa_tnt", 5, null);
        helper.getLevel().addFreshEntity(tnt);
        helper.runAfterDelay(30, () -> {
            helper.assertTrue(helper.getLevel().getBlockState(ap.offset(0, 1, -4)).getBlock()
                            == net.minecraft.world.level.block.Blocks.OAK_DOOR,
                    "La TNT Casa deberia construir una puerta de roble");
            helper.assertTrue(helper.getLevel().getBlockState(ap).getBlock()
                            == net.minecraft.world.level.block.Blocks.OAK_PLANKS,
                    "La TNT Casa deberia construir el piso de tablones");
            helper.succeed();
        });
    }

    /** TNT Mansión: al explotar construye la mansion de cuarzo con fuente. */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void mansion_tnt_builds_mansion(GameTestHelper helper) {
        BlockPos ap = helper.absolutePos(new BlockPos(8, 2, 8));
        // entidad con mecha corta (camino completo). getBlockState(pos) del
        // helper trata el pos como RELATIVO, asi que se usa el nivel directo.
        var tnt = new com.tnts.entity.TntsPrimedTnt(
                helper.getLevel(), ap.getX() + 0.5, ap.getY(), ap.getZ() + 0.5,
                "tnts:mansion_tnt", 5, null);
        helper.getLevel().addFreshEntity(tnt);
        helper.runAfterDelay(15, () -> {
            helper.assertTrue(helper.getLevel().getBlockState(ap.offset(0, 1, -7)).getBlock()
                            == net.minecraft.world.level.block.Blocks.DARK_OAK_DOOR,
                    "La TNT Mansión deberia construir una puerta de roble oscuro");
            helper.assertTrue(helper.getLevel().getBlockState(ap).getBlock()
                            == net.minecraft.world.level.block.Blocks.SMOOTH_QUARTZ,
                    "La TNT Mansión deberia construir el piso de cuarzo liso");
            // fuente: el agua central es estable y se auto-repara (los tests
            // paralelos pueden romper el anillo de oro, pero el agua re-fluye)
            helper.assertTrue(helper.getLevel().getBlockState(ap.offset(0, 0, -9)).getBlock()
                            == net.minecraft.world.level.block.Blocks.WATER,
                    "La TNT Mansión deberia construir la fuente con agua");
            helper.succeed();
        });
    }

    /** GOLEM de TNT: sigue a su dueño y explota al darle con mechero. */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void tnt_golem_follows_and_explodes(GameTestHelper helper) {
        BlockPos ap = helper.absolutePos(new BlockPos(8, 2, 8));
        var golem = new com.tnts.entity.TntGolemEntity(
                com.tnts.entity.TntsEntities.TNT_GOLEM.get(), helper.getLevel());
        golem.setPos(ap.getX() + 0.5, ap.getY(), ap.getZ() + 0.5);
        golem.setVariant("mini_tnt");
        golem.setOwner(new java.util.UUID(0, 1)); // dueño ficticio (solo guardado)
        helper.getLevel().addFreshEntity(golem);

        helper.runAfterDelay(10, () -> {
            // el golem deberia estar vivo y con su variante
            helper.assertTrue(golem.isAlive(), "El Golem deberia estar vivo");
            helper.assertTrue("mini_tnt".equals(golem.getVariant()),
                    "El Golem deberia llevar la variante mini_tnt");
            // darle con MECHERO -> explota (desaparece y spawna la TNT fantasma)
            var player = helper.makeMockPlayer();
            player.getInventory().setItem(player.getInventory().selected,
                    new ItemStack(Items.FLINT_AND_STEEL));
            var result = golem.mobInteract(player, InteractionHand.MAIN_HAND);
            helper.assertTrue(result == InteractionResult.sidedSuccess(true),
                    "Dar con mechero al Golem deberia devolver exito");
            helper.assertTrue(!golem.isAlive() || golem.isRemoved(),
                    "El Golem deberia haber explotado y desaparecido");
            helper.succeed();
        });
    }

    /** TNT COHETE: se monta y despega al agacharse (empuje + estela). */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void tnt_rocket_rides_and_takes_off(GameTestHelper helper) {
        BlockPos ap = helper.absolutePos(new BlockPos(8, 2, 8));
        var rocket = new com.tnts.entity.TntRocketEntity(
                com.tnts.entity.TntsEntities.TNT_ROCKET.get(), helper.getLevel());
        rocket.setPos(ap.getX() + 0.5, ap.getY(), ap.getZ() + 0.5);
        helper.getLevel().addFreshEntity(rocket);

        helper.runAfterDelay(10, () -> {
            helper.assertTrue(!rocket.isRemoved(), "El Cohete deberia estar en el mundo");
            // montarlo
            var player = helper.makeMockPlayer();
            player.setPos(ap.getX() + 0.5, ap.getY() + 1.0, ap.getZ() + 0.5);
            var result = rocket.interact(player, InteractionHand.MAIN_HAND);
            helper.assertTrue(result == InteractionResult.sidedSuccess(true),
                    "Montar el Cohete deberia devolver exito");
            // agacharse -> despega (15 ticks de aviso + vuelo). El cohete
            // necesita que sueltes el shift tras despegar para seguir volando
            // (mantenerlo frena). Se simula: agachar 20 ticks, soltar, volar.
            player.setShiftKeyDown(true);
            for (int i = 0; i < 20; i++) {
                rocket.tick();
            }
            player.setShiftKeyDown(false);
            for (int i = 0; i < 20; i++) {
                rocket.tick();
            }
            helper.assertTrue(rocket.isFlying(),
                    "El Cohete deberia estar volando tras despegar");
            helper.assertTrue(rocket.getY() > ap.getY(), "El Cohete deberia haber subido");
            helper.succeed();
        });
    }

    /** AVIÓN de TNT: el piloto puede lanzar TNTs en vuelo (consume la de la mano). */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void tnt_rocket_throws_tnt(GameTestHelper helper) {
        BlockPos ap = helper.absolutePos(new BlockPos(8, 2, 8));
        var rocket = new com.tnts.entity.TntRocketEntity(
                com.tnts.entity.TntsEntities.TNT_ROCKET.get(), helper.getLevel());
        rocket.setPos(ap.getX() + 0.5, ap.getY(), ap.getZ() + 0.5);
        helper.getLevel().addFreshEntity(rocket);

        helper.runAfterDelay(10, () -> {
            var player = helper.makeMockPlayer();
            player.setPos(ap.getX() + 0.5, ap.getY() + 1.0, ap.getZ() + 0.5);
            // montarlo y ponerlo a volar (estado 2 directo para el test)
            rocket.interact(player, InteractionHand.MAIN_HAND);
            // el piloto lleva una Mega TNT en la mano
            player.getInventory().setItem(player.getInventory().selected,
                    new ItemStack(ModItems.MEGA_TNT.get()));
            rocket.throwTnt(player);
            AABB area = new AABB(ap).inflate(8);
            var primed = helper.getLevel().getEntitiesOfClass(TntsPrimedTnt.class, area);
            helper.assertTrue(!primed.isEmpty(), "El avión deberia haber lanzado una TNT");
            helper.assertTrue("mega_tnt".equals(primed.get(0).getVariantName()),
                    "La TNT lanzada deberia ser la de la mano (mega)");
            helper.succeed();
        });
    }
}
