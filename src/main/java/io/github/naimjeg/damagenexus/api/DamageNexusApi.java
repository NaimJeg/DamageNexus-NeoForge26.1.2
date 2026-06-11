package io.github.naimjeg.damagenexus.api;

import com.mojang.serialization.MapCodec;
import io.github.naimjeg.damagenexus.api.item.DamageNexusItemApi;
import io.github.naimjeg.damagenexus.api.item.DamageNexusItemEntries;
import io.github.naimjeg.damagenexus.api.rule.*;
import io.github.naimjeg.damagenexus.api.rule.provider.StaticDamageRuleProvider;
import io.github.naimjeg.damagenexus.core.lifecycle.DamageNexusLifecycle;
import io.github.naimjeg.damagenexus.core.registry.PreMultiplierBucketRegistry;
import io.github.naimjeg.damagenexus.registry.DamagePhaseProcessorRegistry;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleConditionTypes;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleOperationTypes;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleProviders;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class DamageNexusApi {

    private DamageNexusApi() {
    }

    public static void registerCondition(
            Identifier id,
            MapCodec<? extends DamageRuleCondition> codec
    ) {
        DamageNexusLifecycle.requireRegistering("registerCondition");
        DamageRuleConditionTypes.register(id, codec);
    }

    public static void registerOperation(
            Identifier id,
            MapCodec<? extends DamageRuleOperation> codec
    ) {
        DamageNexusLifecycle.requireRegistering("registerOperation");
        DamageRuleOperationTypes.register(id, codec);
    }

    public static void registerRuleProvider(DamageRuleProvider provider) {
        DamageNexusLifecycle.requireRegistering("registerRuleProvider");
        DamageRuleProviders.register(provider);
    }

    public static void registerGlobalRule(DamageRuleDefinition rule) {
        DamageNexusLifecycle.requireRegistering("registerGlobalRule");
        DamageRuleValidator.requireValid(
                rule,
                "java_api/register_global_rule"
        );

        registerRuleProvider(new StaticDamageRuleProvider(rule));
    }

    public static int registerPreMultiplierBucket(Identifier id) {
        DamageNexusLifecycle.requireRegistering("registerPreMultiplierBucket");
        return PreMultiplierBucketRegistry.registerPreMultiplierBucket(id);
    }

    public static void registerPhaseProcessor(
            DamagePhaseProcessor processor
    ) {
        DamageNexusLifecycle.requireRegistering("registerPhaseProcessor");
        DamagePhaseProcessorRegistry.registerExternal(processor);
    }

    public static DamageNexusItemEntries getItemEntries(ItemStack stack) {
        return DamageNexusItemApi.get(stack);
    }

    public static boolean setItemEntries(
            ItemStack stack,
            DamageNexusItemEntries entries
    ) {
        return DamageNexusItemApi.set(stack, entries);
    }

    public static boolean clearItemEntries(ItemStack stack) {
        return DamageNexusItemApi.clear(stack);
    }
}
