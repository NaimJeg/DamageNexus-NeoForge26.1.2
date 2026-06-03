package io.github.naimjeg.damagenexus.client.tooltip;

import io.github.naimjeg.damagenexus.api.rule.DamageRuleCondition;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleOperation;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

public record VanillaBridgeTooltipSpec(
        Identifier source,
        Component displayName,
        List<DamageRuleOperation> operations,
        List<DamageRuleCondition> conditions
) {}