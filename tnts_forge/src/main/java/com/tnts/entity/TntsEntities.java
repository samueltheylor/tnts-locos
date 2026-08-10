package com.tnts.entity;

import com.tnts.TntsMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class TntsEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, TntsMod.MODID);

    public static final RegistryObject<EntityType<TntsPrimedTnt>> PRIMED_TNT =
            ENTITIES.register("primed_tnt", () -> EntityType.Builder
                    .<TntsPrimedTnt>of(TntsPrimedTnt::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(16)
                    .updateInterval(10)
                    .build("primed_tnt"));

    public static final RegistryObject<EntityType<TntArrowEntity>> TNT_ARROW =
            ENTITIES.register("tnt_arrow", () -> EntityType.Builder
                    .<TntArrowEntity>of(TntArrowEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("tnt_arrow"));

    public static final RegistryObject<EntityType<BlackHoleEntity>> BLACK_HOLE =
            ENTITIES.register("black_hole", () -> EntityType.Builder
                    .<BlackHoleEntity>of(BlackHoleEntity::new, MobCategory.MISC)
                    .sized(0.9F, 0.9F)
                    .clientTrackingRange(16)
                    .updateInterval(5)
                    .build("black_hole"));
}
