package io.github.naimjeg.damagenexus.core.rule;

import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.api.rule.*;
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

        for (RuntimeDamageRule runtimeRule : input) {
            DamageRuleDefinition rule = runtimeRule.definition();

            switch (rule.stacking()) {
                case STACK -> stacked.add(runtimeRule);

                case UNIQUE_SOURCE -> mergeWithTrace(
                        uniqueSource,
                        keyOf(runtimeRule),
                        runtimeRule,
                        traces,
                        DamageRuleStacking.UNIQUE_SOURCE,
                        DamageRuleStackingResolver::chooseHigherPriority
                );

                case HIGHEST_VALUE -> mergeWithTrace(
                        highestValue,
                        keyOf(runtimeRule),
                        runtimeRule,
                        traces,
                        DamageRuleStacking.HIGHEST_VALUE,
                        DamageRuleStackingResolver::chooseHigherValue
                );

                case LOWEST_VALUE -> mergeWithTrace(
                        lowestValue,
                        keyOf(runtimeRule),
                        runtimeRule,
                        traces,
                        DamageRuleStacking.LOWEST_VALUE,
                        DamageRuleStackingResolver::chooseLowestValue
                );

                case REPLACE -> mergeWithTrace(
                        replace,
                        keyOf(runtimeRule),
                        runtimeRule,
                        traces,
                        DamageRuleStacking.REPLACE,
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
            DamageRuleStacking policy,
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

        DamageRuleDefinition candidateDefinition = candidate.definition();
        DamageRuleDefinition chosenDefinition = chosen.definition();
        DamageRuleDefinition droppedDefinition = dropped.definition();

        traces.add(new StackingTrace(
                candidateDefinition.phase(),
                chosenDefinition.id(),
                droppedDefinition.id(),
                policy,
                stackingValue(chosenDefinition),
                stackingValue(droppedDefinition)
        ));
    }

    private static RuntimeDamageRule chooseHigherPriority(
            RuntimeDamageRule existing,
            RuntimeDamageRule candidate
    ) {
        if (candidate.definition().priority() > existing.definition().priority()) {
            return candidate;
        }

        return existing;
    }

    private static RuntimeDamageRule chooseReplacement(
            RuntimeDamageRule existing,
            RuntimeDamageRule candidate
    ) {
        if (candidate.definition().priority() >= existing.definition().priority()) {
            return candidate;
        }

        return existing;
    }

    private static RuntimeDamageRule chooseHigherValue(
            RuntimeDamageRule existing,
            RuntimeDamageRule candidate
    ) {
        float existingValue = stackingValue(existing.definition());
        float candidateValue = stackingValue(candidate.definition());

        int valueCompare = Float.compare(candidateValue, existingValue);

        if (valueCompare > 0) {
            return candidate;
        }

        if (valueCompare == 0
                && candidate.definition().priority() > existing.definition().priority()) {
            return candidate;
        }

        return existing;
    }

    private static RuntimeDamageRule chooseLowestValue(
            RuntimeDamageRule existing,
            RuntimeDamageRule candidate
    ) {
        float existingValue = stackingValue(existing.definition());
        float candidateValue = stackingValue(candidate.definition());

        int valueCompare = Float.compare(candidateValue, existingValue);

        if (valueCompare < 0) {
            return candidate;
        }

        if (valueCompare == 0
                && candidate.definition().priority() > existing.definition().priority()) {
            return candidate;
        }

        return existing;
    }

    private static StackingKey keyOf(RuntimeDamageRule runtimeRule) {
        DamageRuleDefinition rule = runtimeRule.definition();

        return new StackingKey(
                rule.stackingKey(),
                rule.phase(),
                rule.role(),
                primaryOperationType(rule)
        );
    }

    private static Identifier primaryOperationType(DamageRuleDefinition rule) {
        if (rule.operations().isEmpty()) {
            return rule.id();
        }

        return rule.operations().getFirst().type();
    }

    private static float stackingValue(DamageRuleDefinition rule) {
        return rule.operations()
                .stream()
                .map(DamageRuleOperation::stackingValue)
                .max(Float::compare)
                .orElse(0.0f);
    }

    private record StackingKey(
            Identifier group,
            DamagePhase phase,
            DamageRuleRole role,
            Identifier operationType
    ) {}
}