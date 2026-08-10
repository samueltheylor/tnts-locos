package com.tnts;

import com.tnts.config.TntDefaults;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

/** Sonidos propios del mod (sintetizados, ver tools/gen_sounds.py). */
public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, TntsMod.MODID);

    private static final Map<String, RegistryObject<SoundEvent>> FUSE = new HashMap<>();
    private static final Map<String, RegistryObject<SoundEvent>> EXPLODE = new HashMap<>();

    /** Pitido de cuenta atras (TNT Trampa). */
    public static final RegistryObject<SoundEvent> BEEP = register("block.tnt.beep");
    /** Click del Detonador Remoto. */
    public static final RegistryObject<SoundEvent> DETONATOR_CLICK = register("item.detonator.click");
    /** Rafaga del Detonador al detonar muchas TNTs de golpe (10+). */
    public static final RegistryObject<SoundEvent> DETONATOR_BURST = register("item.detonator.burst");
    /** Disparo del Lanzador de TNT. */
    public static final RegistryObject<SoundEvent> LAUNCHER_SHOOT = register("item.launcher.shoot");
    /** Remolino grave de la bola negra (Agujero Negro), en bucle. */
    public static final RegistryObject<SoundEvent> BLACK_HOLE_LOOP = register("entity.black_hole.loop");

    static {
        for (String name : TntDefaults.DEFAULTS.keySet()) {
            FUSE.put(name, register("block.tnt.fuse." + name));
            EXPLODE.put(name, register("block.tnt.explode." + name));
        }
    }

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(name,
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(TntsMod.MODID, name)));
    }

    public static SoundEvent fuse(String variant) {
        RegistryObject<SoundEvent> sound = FUSE.get(variant);
        return sound != null ? sound.get() : BEEP.get();
    }

    public static SoundEvent explode(String variant) {
        RegistryObject<SoundEvent> sound = EXPLODE.get(variant);
        return sound != null ? sound.get() : BEEP.get();
    }
}
