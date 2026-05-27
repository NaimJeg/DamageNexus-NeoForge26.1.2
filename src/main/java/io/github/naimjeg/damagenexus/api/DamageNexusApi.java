package io.github.naimjeg.damagenexus.api;

import com.mojang.serialization.MapCodec;
import io.github.naimjeg.damagenexus.api.rule.*;
import io.github.naimjeg.damagenexus.api.rule.provider.StaticDamageRuleProvider;
import io.github.naimjeg.damagenexus.core.registry.PreMultiplierBucketRegistry;
import io.github.naimjeg.damagenexus.registry.DamagePhaseProcessorRegistry;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleConditionTypes;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleOperationTypes;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleProviders;
import net.minecraft.resources.Identifier;

public final class DamageNexusApi {

    private DamageNexusApi() {}

    public static void registerCondition(
            Identifier id,
            MapCodec<? extends DamageRuleCondition> codec
    ) {
        DamageRuleConditionTypes.register(id, codec);
    }

    public static void registerOperation(
            Identifier id,
            MapCodec<? extends DamageRuleOperation> codec
    ) {
        DamageRuleOperationTypes.register(id, codec);
    }

    public static void registerRuleProvider(DamageRuleProvider provider) {
        DamageRuleProviders.register(provider);
    }

    public static void registerGlobalRule(DamageRuleDefinition rule) {
        DamageRuleValidator.requireValid(
                rule,
                "java_api/register_global_rule"
        );

        registerRuleProvider(new StaticDamageRuleProvider(rule));
    }
    
    public static int registerPreMultiplierBucket(Identifier id) {
        return PreMultiplierBucketRegistry.registerPreMultiplierBucket(id);
    }

    public static void registerPhaseProcessor(
            DamagePhaseProcessor processor
    ) {
        DamagePhaseProcessorRegistry.registerExternal(processor);
    }
}