package io.github.naimjeg.damagenexus.registry;

import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.api.rule.affix.DamageAffixDefinition;
import io.github.naimjeg.damagenexus.api.rule.entry.DamageEntryDefinition;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public final class ModDataComponents {

    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, DamageNexus.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<DamageAffixDefinition>>> DAMAGE_AFFIXES =
            COMPONENTS.register("damage_affixes", () ->
                    DataComponentType.<List<DamageAffixDefinition>>builder()
                            .persistent(DamageAffixDefinition.CODEC.listOf())
                            .networkSynchronized(ByteBufCodecs.fromCodec(DamageAffixDefinition.CODEC.listOf()))
                            .cacheEncoding()
                            .build()
            );

    public static final DeferredHolder<
            DataComponentType<?>,
            DataComponentType<List<DamageEntryDefinition>>
            > DAMAGE_ENTRIES =
            COMPONENTS.register(
                    "damage_entries",
                    () -> DataComponentType
                            .<List<DamageEntryDefinition>>builder()
                            .persistent(DamageEntryDefinition.CODEC.listOf())
                            .networkSynchronized(ByteBufCodecs.fromCodec(
                                    DamageEntryDefinition.CODEC.listOf()
                            ))
                            .build()
            );

    private ModDataComponents() {
    }

    public static void register(IEventBus eventBus) {
        COMPONENTS.register(eventBus);
    }
}
