package io.github.naimjeg.damagenexus.core.rule;

import io.github.naimjeg.damagenexus.api.affix.AffixEntry;
import io.github.naimjeg.damagenexus.api.affix.AffixEffect;
import io.github.naimjeg.damagenexus.api.affix.effect.*;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleRole;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleStacking;
import io.github.naimjeg.damagenexus.api.rule.RuntimeDamageRule;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DamageRuleStackingResolver {

    private DamageRuleStackingResolver() {}

    public static DamageRuleStackingResult resolve(List<RuntimeDamageRule> input) {
        if (input.isEmpty()) {
            return new DamageRuleStackingResult(input, List.of());
        }

        List<RuntimeDamageRule> stacked = new ArrayList<>();
        List<StackingTrace> traces = new ArrayList<>();

        Map<StackingKey, RuntimeDamageRule> uniqueSource = new LinkedHashMap<>();
        Map<StackingKey, RuntimeDamageRule> highestValue = new LinkedHashMap<>();
        Map<StackingKey, RuntimeDamageRule> lowestValue = new LinkedHashMap<>();
        Map<StackingKey, RuntimeDamageRule> replace = new LinkedHashMap<>();

        for (RuntimeDamageRule rule : input) {
            switch (rule.entry().stacking()) {
                case STACK -> stacked.add(rule);

                case UNIQUE_SOURCE -> mergeWithTrace(
                        uniqueSource,
                        keyOf(rule),
                        rule,
                        traces,
                        "unique_source",
                        DamageRuleStackingResolver::chooseHigherPriority
                );

                case HIGHEST_VALUE -> mergeWithTrace(
                        highestValue,
                        keyOf(rule),
                        rule,
                        traces,
                        "highest_value",
                        DamageRuleStackingResolver::chooseHigherValue
                );

                case LOWEST_VALUE -> mergeWithTrace(
                        lowestValue,
                        keyOf(rule),
                        rule,
                        traces,
                        "lowest_value",
                        DamageRuleStackingResolver::chooseLowestValue
                );

                case REPLACE -> mergeWithTrace(
                        replace,
                        keyOf(rule),
                        rule,
                        traces,
                        "replace",
                        DamageRuleStackingResolver::chooseReplacement
                );
            }
        }

        stacked.addAll(uniqueSource.values());
        stacked.addAll(highestValue.values());
        stacked.addAll(lowestValue.values());
        stacked.addAll(replace.values());

        return new DamageRuleStackingResult(stacked, traces);
    }

    @FunctionalInterface
    private interface RuleChooser {
        RuntimeDamageRule choose(RuntimeDamageRule existing, RuntimeDamageRule candidate);
    }

    private static void mergeWithTrace(
            Map<StackingKey, RuntimeDamageRule> map,
            StackingKey key,
            RuntimeDamageRule candidate,
            List<StackingTrace> traces,
            String reason,
            RuleChooser chooser
    ) {
        RuntimeDamageRule existing = map.get(key);

        if (existing == null) {
            map.put(key, candidate);
            return;
        }

        RuntimeDamageRule chosen = chooser.choose(existing, candidate);
        RuntimeDamageRule dropped = chosen == existing ? candidate : existing;

        map.put(key, chosen);

        traces.add(new StackingTrace(
                candidate.entry().phase(),
                chosen.entry().id(),
                dropped.entry().id(),
                reason,
                stackingValue(chosen.entry()),
                stackingValue(dropped.entry())
        ));
    }

    private static RuntimeDamageRule chooseHigherPriority(
            RuntimeDamageRule existing,
            RuntimeDamageRule candidate
    ) {
        if (candidate.entry().priority() > existing.entry().priority()) {
            return candidate;
        }

        return existing;
    }

    private static RuntimeDamageRule chooseReplacement(
            RuntimeDamageRule existing,
            RuntimeDamageRule candidate
    ) {
        if (candidate.entry().priority() >= existing.entry().priority()) {
            return candidate;
        }

        return existing;
    }

    private static RuntimeDamageRule chooseHigherValue(
            RuntimeDamageRule existing,
            RuntimeDamageRule candidate
    ) {
        float existingValue = stackingValue(existing.entry());
        float candidateValue = stackingValue(candidate.entry());

        if (candidateValue > existingValue) {
            return candidate;
        }

        if (candidateValue == existingValue
                && candidate.entry().priority() > existing.entry().priority()) {
            return candidate;
        }

        return existing;
    }

    private static RuntimeDamageRule chooseLowestValue(
            RuntimeDamageRule existing,
            RuntimeDamageRule candidate
    ) {
        float existingValue = stackingValue(existing.entry());
        float candidateValue = stackingValue(candidate.entry());

        if (candidateValue < existingValue) {
            return candidate;
        }

        if (candidateValue == existingValue
                && candidate.entry().priority() > existing.entry().priority()) {
            return candidate;
        }

        return existing;
    }

    private static StackingKey keyOf(RuntimeDamageRule rule) {
        AffixEntry entry = rule.entry();

        Identifier group =
                entry.stackingGroup().orElse(entry.id());

        Identifier operationType =
                primaryOperationType(entry);

        return new StackingKey(
                group,
                entry.phase(),
                entry.role(),
                operationType
        );
    }

    private static Identifier primaryOperationType(AffixEntry entry) {
        if (entry.effects().isEmpty()) {
            return entry.id();
        }

        return entry.effects().get(0).type();
    }

    private static float stackingValue(AffixEntry entry) {
        if (entry.effects().isEmpty()) {
            return 0.0f;
        }

        /*
         * 第一版约定：
         * HIGHEST_VALUE / LOWEST_VALUE 只比较第一个 effect 的数值。
         *
         * 长期建议：
         * 一条 rule 只放一个 operation/effect。
         * 多效果词条用多个 rule，共享同一个 source/id 或 stacking_group。
         */
        return effectValue(entry.effects().get(0));
    }

    private static float effectValue(AffixEffect effect) {
        return switch (effect) {
            case AddBaseDamageEffect e -> e.value();
            case AddChannelPreMultiplierEffect e -> e.value();
            case AddChannelPostMultiplierEffect e -> e.value();
            case AddGlobalPostMultiplierEffect e -> e.value();
            case OverrideFinalDamageEffect e -> e.value();
            case AddTemporaryResistanceEffect e -> e.value();
            default -> 0.0f;
        };
    }

    private record StackingKey(
            Identifier group,
            DamagePhase phase,
            DamageRuleRole role,
            Identifier operationType
    ) {}
}