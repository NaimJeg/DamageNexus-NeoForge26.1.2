package io.github.naimjeg.damagenexus.api.rule;

import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;

import java.util.List;

public interface DamageRuleProvider {

    default boolean supportsPhase(DamagePhase phase) {
        return true;
    }

    void collect(
            DamageNexusContext ctx,
            DamagePhase phase,
            List<RuntimeDamageRule> out
    );
}