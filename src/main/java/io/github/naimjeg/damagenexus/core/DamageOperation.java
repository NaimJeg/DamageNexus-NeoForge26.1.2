package io.github.naimjeg.damagenexus.core;

import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.core.trace.DamageMutationType;

public record DamageOperation(
        String source,
        DamagePhase phase,
        DamageMutationType type,
        float value
) {}