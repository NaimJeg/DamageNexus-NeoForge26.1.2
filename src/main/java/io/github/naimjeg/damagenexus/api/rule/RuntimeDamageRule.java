package io.github.naimjeg.damagenexus.api.rule;

import io.github.naimjeg.damagenexus.api.affix.AffixEntry;

public record RuntimeDamageRule(
        AffixEntry entry,
        RuleExecutionContext executionContext
) {}