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

    public static final RegistryObject<EntityType<MeteorEntity>> METEOR =
            ENTITIES.register("meteor", () -> EntityType.Builder
                    .<MeteorEntity>of(MeteorEntity::new, MobCategory.MISC)
                    .sized(1.2F, 1.2F)
                    .clientTrackingRange(16)
                    .updateInterval(5)
                    .build("meteor"));

    public static final RegistryObject<EntityType<StormCloudEntity>> STORM_CLOUD =
            ENTITIES.register("storm_cloud", () -> EntityType.Builder
                    .<StormCloudEntity>of(StormCloudEntity::new, MobCategory.MISC)
                    .sized(4.0F, 1.5F)
                    .clientTrackingRange(16)
                    .updateInterval(5)
                    .build("storm_cloud"));

    public static final RegistryObject<EntityType<ShockwaveEntity>> SHOCKWAVE =
            ENTITIES.register("shockwave", () -> EntityType.Builder
                    .<ShockwaveEntity>of(ShockwaveEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .clientTrackingRange(16)
                    .updateInterval(5)
                    .build("shockwave"));

    public static final RegistryObject<EntityType<SupernovaEntity>> SUPERNOVA =
            ENTITIES.register("supernova", () -> EntityType.Builder
                    .<SupernovaEntity>of(SupernovaEntity::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(16)
                    .updateInterval(5)
                    .build("supernova"));

    /** Efecto 3D generico para TODAS las TNTs: renderiza el bloque real. */
    public static final RegistryObject<EntityType<TntBlastEntity>> TNT_BLAST =
            ENTITIES.register("tnt_blast", () -> EntityType.Builder
                    .<TntBlastEntity>of(TntBlastEntity::new, MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(16)
                    .updateInterval(3)
                    .build("tnt_blast"));
}
