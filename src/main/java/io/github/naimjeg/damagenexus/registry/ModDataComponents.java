package io.github.naimjeg.damagenexus.registry;

import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.api.affix.AffixEntry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public final class ModDataComponents {

    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, DamageNexus.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<AffixEntry>>> ITEM_AFFIXES =
            COMPONENTS.register("item_affixes", () ->
                    DataComponentType.<List<AffixEntry>>builder()
                            .persistent(AffixEntry.CODEC.listOf())
                            .cacheEncoding()
                            .build()
            );

    private ModDataComponents() {}

    public static void register(IEventBus eventBus) {
        COMPONENTS.register(eventBus);
    }
}