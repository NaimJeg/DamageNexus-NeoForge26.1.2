package io.github.naimjeg.damagenexus.registry;

import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleDefinition;
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

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<DamageRuleDefinition>>> DAMAGE_RULES =
            COMPONENTS.register("damage_rules", () ->
                    DataComponentType.<List<DamageRuleDefinition>>builder()
                            .persistent(DamageRuleDefinition.CODEC.listOf())
                            .networkSynchronized(ByteBufCodecs.fromCodec(DamageRuleDefinition.CODEC.listOf()))
                            .cacheEncoding()
                            .build()
            );

    private ModDataComponents() {}

    public static void register(IEventBus eventBus) {
        COMPONENTS.register(eventBus);
    }
}