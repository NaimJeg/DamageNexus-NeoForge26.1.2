package io.github.naimjeg.damagenexus.api.event;

import com.mojang.serialization.MapCodec;
import io.github.naimjeg.damagenexus.api.DamageNexusApi;
import io.github.naimjeg.damagenexus.api.DamagePhaseProcessor;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleCondition;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleDefinition;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleOperation;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleProvider;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;

/**
 * Fired on the NeoForge event bus during DamageNexus common setup,
 * after built-in pre-multiplier buckets are registered and before
 * the pre-multiplier bucket registry is frozen.
 * <p>
 * Listen on the NeoForge/GAME bus, not the mod event bus.
 */
public final class DamageNexusRegisterEvent extends Event {

    public void registerCondition(
            Identifier id,
            MapCodec<? extends DamageRuleCondition> codec
    ) {
        DamageNexusApi.registerCondition(id, codec);
    }

    public void registerOperation(
            Identifier id,
            MapCodec<? extends DamageRuleOperation> codec
    ) {
        DamageNexusApi.registerOperation(id, codec);
    }

    public int registerPreMultiplierBucket(Identifier id) {
        return DamageNexusApi.registerPreMultiplierBucket(id);
    }

    public void registerRuleProvider(DamageRuleProvider provider) {
        DamageNexusApi.registerRuleProvider(provider);
    }

    public void registerGlobalRule(DamageRuleDefinition rule) {
        DamageNexusApi.registerGlobalRule(rule);
    }

    public void registerPhaseProcessor(DamagePhaseProcessor processor) {
        DamageNexusApi.registerPhaseProcessor(processor);
    }
}
