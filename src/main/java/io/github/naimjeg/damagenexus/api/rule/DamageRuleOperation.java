package io.github.naimjeg.damagenexus.api.rule;

import com.mojang.serialization.Codec;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.core.pipeline.DamageMutationResult;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleOperationTypes;
import net.minecraft.resources.Identifier;

import java.util.Set;

public interface DamageRuleOperation {

    Codec<DamageRuleOperation> CODEC =
            Identifier.CODEC.dispatch(
                    "type",
                    DamageRuleOperation::type,
                    DamageRuleOperationTypes::codec
            );

    Identifier type();

    /**
     * Legacy operation entry point.
     *
     * Kept for source compatibility with older Java API users.
     */
    void apply(DamageNexusContext ctx);

    /**
     * Result-aware operation entry point.
     *
     * Built-in operations should override this and call the corresponding
     * DamageNexusContext.try* method.
     *
     * External legacy operations that only implement apply(ctx) still work,
     * but their result is conservatively reported as APPLIED.
     */
    default DamageMutationResult applyWithResult(DamageNexusContext ctx) {
        apply(ctx);
        return DamageMutationResult.APPLIED;
    }

    default float stackingValue() {
        return 0.0f;
    }

    default Set<DamagePhase> supportedPhases() {
        return Set.of();
    }

    default boolean supportsPhase(DamagePhase phase) {
        Set<DamagePhase> phases = supportedPhases();
        return phases.isEmpty() || phases.contains(phase);
    }
}