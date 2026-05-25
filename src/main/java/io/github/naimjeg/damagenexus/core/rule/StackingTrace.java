package io.github.naimjeg.damagenexus.core.rule;

import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import net.minecraft.resources.Identifier;

public record StackingTrace(
        DamagePhase phase,
        Identifier kept,
        Identifier dropped,
        String reason,
        float keptValue,
        float droppedValue
) {}